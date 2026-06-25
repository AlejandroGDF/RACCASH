package com.raccooncash.api.presupuesto;

import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
import com.raccooncash.api.transaccion.Transaccion;
import com.raccooncash.api.transaccion.TransaccionRepositorio;
import com.raccooncash.api.transaccion.TipoTransaccion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PresupuestoServicio {

    private final PresupuestoRepositorio budgetRepository;
    private final TransaccionRepositorio transactionRepository;
    private final LimiteCategoriaPresupuestoRepositorio limitRepository;

    public PresupuestoServicio(PresupuestoRepositorio budgetRepository,
                               TransaccionRepositorio transactionRepository,
                               LimiteCategoriaPresupuestoRepositorio limitRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.limitRepository = limitRepository;
    }

    @Transactional(readOnly = true)
    public List<PresupuestoRespuesta> getAllBudgets() {
        return budgetRepository.findAllByActiveTrueOrderByStartDateDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PresupuestoRespuesta getBudgetById(Long id) {
        return toResponse(findActiveBudget(id));
    }

    @Transactional
    public PresupuestoRespuesta createBudget(PresupuestoSolicitud request) {
        validateRequest(request);

        Presupuesto budget = new Presupuesto();
        fillBudget(budget, request);
        budget.setActive(true);

        Presupuesto savedBudget = budgetRepository.save(budget);
        return toResponse(savedBudget);
    }

    @Transactional
    public PresupuestoRespuesta updateBudget(Long id, PresupuestoSolicitud request) {
        validateRequest(request);

        Presupuesto budget = findActiveBudget(id);
        fillBudget(budget, request);

        Presupuesto updatedBudget = budgetRepository.save(budget);
        return toResponse(updatedBudget);
    }

    @Transactional
    public void deleteBudget(Long id) {
        Presupuesto budget = findActiveBudget(id);
        budget.setActive(false);
        budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public ResumenPresupuestoRespuesta getBudgetSummary(Long id) {
        Presupuesto budget = findActiveBudget(id);
        BigDecimal spent = calculateCurrentAmount(budget);

        BigDecimal remaining = budget.getAmountLimit().subtract(spent);
        BigDecimal percentageUsed = spent
                .multiply(BigDecimal.valueOf(100))
                .divide(budget.getAmountLimit(), 2, RoundingMode.HALF_UP);

        return new ResumenPresupuestoRespuesta(
                budget.getName(),
                budget.getAmountLimit(),
                spent,
                remaining,
                percentageUsed);
    }

    private void fillBudget(Presupuesto budget, PresupuestoSolicitud request) {
        budget.setName(request.getNombre());
        budget.setAmountLimit(BigDecimal.valueOf(request.getMonto()));
        budget.setPeriodType(request.getTipoPeriodo());
        budget.setPeriodValue(request.getValorPeriodo());
        budget.setStartDate(request.getFechaInicio());
        budget.setEndDate(calculateEndDate(request.getFechaInicio(), request.getTipoPeriodo(), request.getValorPeriodo()));
        budget.setColor(request.getColor());
        budget.setExpense(request.getEsGasto());
        budget.setIncludeAllTransactions(request.getIncluirTodasLasTransacciones());
    }

    private void validateRequest(PresupuestoSolicitud request) {
        if (request.getMonto() == null || request.getMonto() <= 0) {
            throw new SolicitudIncorrectaException("El monto debe ser mayor a cero");
        }
        if (request.getTipoPeriodo() == null) {
            throw new SolicitudIncorrectaException("El periodo es obligatorio");
        }
        if (request.getValorPeriodo() == null || request.getValorPeriodo() <= 0) {
            throw new SolicitudIncorrectaException("El valor del periodo debe ser mayor a cero");
        }
        if (request.getFechaInicio() == null) {
            throw new SolicitudIncorrectaException("La fecha inicial es obligatoria");
        }
        if (request.getEsGasto() == null) {
            throw new SolicitudIncorrectaException("El tipo de presupuesto es obligatorio");
        }
        if (request.getIncluirTodasLasTransacciones() == null) {
            throw new SolicitudIncorrectaException("Debe indicar si se incluyen todas las transacciones");
        }
    }

    private LocalDate calculateEndDate(LocalDate startDate, TipoPeriodoPresupuesto periodType, Integer periodValue) {
        return switch (periodType) {
            case DIARIO -> startDate.plusDays(periodValue).minusDays(1);
            case SEMANAL -> startDate.plusWeeks(periodValue).minusDays(1);
            case MENSUAL -> startDate.plusMonths(periodValue).minusDays(1);
            case ANUAL -> startDate.plusYears(periodValue).minusDays(1);
            case PERSONALIZADO -> startDate.plusDays(periodValue).minusDays(1);
        };
    }

    private PresupuestoRespuesta toResponse(Presupuesto budget) {
        return new PresupuestoRespuesta(budget, calculateCurrentAmount(budget));
    }

    private BigDecimal calculateCurrentAmount(Presupuesto budget) {
        TipoTransaccion transactionType = Boolean.TRUE.equals(budget.getExpense()) ? TipoTransaccion.EXPENSE : null;
        List<Transaccion> transactions = transactionRepository.findWithFilters(
                null,
                null,
                transactionType,
                budget.getStartDate().atStartOfDay(),
                budget.getEndDate().atTime(LocalTime.MAX));

        Set<Long> categoryIds = Boolean.TRUE.equals(budget.getIncludeAllTransactions())
                ? Set.of()
                : limitRepository.findAllByBudgetId(budget.getId())
                        .stream()
                        .map(limit -> limit.getCategory().getId())
                        .collect(Collectors.toSet());

        return transactions.stream()
                .filter(transaction -> shouldIncludeTransaction(budget, transaction, categoryIds))
                .map(Transaccion::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean shouldIncludeTransaction(Presupuesto budget, Transaccion transaction, Set<Long> categoryIds) {
        if (Boolean.TRUE.equals(budget.getExpense())) {
            if (transaction.getType() != TipoTransaccion.EXPENSE) {
                return false;
            }
            if (Boolean.TRUE.equals(budget.getIncludeAllTransactions())) {
                return true;
            }
            return transaction.getCategory() != null && categoryIds.contains(transaction.getCategory().getId());
        }

        if (!Boolean.TRUE.equals(budget.getIncludeAllTransactions()) && !categoryIds.isEmpty()) {
            return transaction.getType() == TipoTransaccion.INCOME
                    && transaction.getCategory() != null
                    && categoryIds.contains(transaction.getCategory().getId());
        }

        return transaction.getSavingGoal() != null;
    }

    Presupuesto findActiveBudget(Long id) {
        return budgetRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));
    }
}
