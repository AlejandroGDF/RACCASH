package com.raccooncash.api.reporte;

import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import com.raccooncash.api.transaccion.Transaccion;
import com.raccooncash.api.transaccion.TransaccionRepositorio;
import com.raccooncash.api.transaccion.TipoTransaccion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteServicio {

    private final TransaccionRepositorio transactionRepository;

    public ReporteServicio(TransaccionRepositorio transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public ReporteMensualRespuesta getMonthlyReport(int year, int month) {
        if (month < 1 || month > 12) {
            throw new SolicitudIncorrectaException("El mes debe estar entre 1 y 12");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        BigDecimal income = sumByType(TipoTransaccion.INCOME, from, to);
        BigDecimal expense = sumByType(TipoTransaccion.EXPENSE, from, to);

        return new ReporteMensualRespuesta(year, month, income, expense);
    }

    @Transactional(readOnly = true)
    public List<ReporteCategoriaRespuesta> getByCategory(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        List<Transaccion> expenses = transactionRepository.findWithFilters(
                null,
                null,
                TipoTransaccion.EXPENSE,
                startOfDay(from),
                endOfDay(to));

        Map<Long, ReporteCategoriaRespuesta> reportMap = new LinkedHashMap<>();
        for (Transaccion transaction : expenses) {
            Long categoryId = transaction.getCategory() != null ? transaction.getCategory().getId() : null;
            String categoryName = transaction.getCategory() != null ? transaction.getCategory().getName() : "Sin categoria";
            ReporteCategoriaRespuesta report = reportMap.computeIfAbsent(categoryId,
                    id -> new ReporteCategoriaRespuesta(categoryId, categoryName));
            report.addExpense(transaction.getAmount());
        }

        return new ArrayList<>(reportMap.values());
    }

    @Transactional(readOnly = true)
    public List<ReporteCuentaRespuesta> getByAccount(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        List<Transaccion> transactions = transactionRepository.findWithFilters(
                null,
                null,
                null,
                startOfDay(from),
                endOfDay(to));

        Map<Long, ReporteCuentaRespuesta> reportMap = new LinkedHashMap<>();
        for (Transaccion transaction : transactions) {
            if (transaction.getType() == TipoTransaccion.INCOME) {
                ReporteCuentaRespuesta report = accountReport(reportMap, transaction.getAccount().getId(), transaction.getAccount().getName());
                report.addIncome(transaction.getAmount());
            } else if (transaction.getType() == TipoTransaccion.EXPENSE) {
                ReporteCuentaRespuesta report = accountReport(reportMap, transaction.getAccount().getId(), transaction.getAccount().getName());
                report.addExpense(transaction.getAmount());
            } else if (transaction.getType() == TipoTransaccion.TRANSFER) {
                ReporteCuentaRespuesta source = accountReport(reportMap, transaction.getAccount().getId(), transaction.getAccount().getName());
                source.addTransferOut(transaction.getAmount());

                if (transaction.getToAccount() != null) {
                    ReporteCuentaRespuesta destination = accountReport(reportMap, transaction.getToAccount().getId(), transaction.getToAccount().getName());
                    destination.addTransferIn(transaction.getAmount());
                }
            }
        }

        return new ArrayList<>(reportMap.values());
    }

    @Transactional(readOnly = true)
    public ReporteFlujoCajaRespuesta getCashFlow(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        BigDecimal income = sumByType(TipoTransaccion.INCOME, from, to);
        BigDecimal expense = sumByType(TipoTransaccion.EXPENSE, from, to);
        return new ReporteFlujoCajaRespuesta(from, to, income, expense);
    }

    private ReporteCuentaRespuesta accountReport(Map<Long, ReporteCuentaRespuesta> reportMap, Long accountId, String accountName) {
        return reportMap.computeIfAbsent(accountId, id -> new ReporteCuentaRespuesta(accountId, accountName));
    }

    private BigDecimal sumByType(TipoTransaccion type, LocalDate from, LocalDate to) {
        return transactionRepository.findWithFilters(null, null, type, startOfDay(from), endOfDay(to))
                .stream()
                .map(Transaccion::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new SolicitudIncorrectaException("Las fechas from y to son obligatorias");
        }
        if (to.isBefore(from)) {
            throw new SolicitudIncorrectaException("La fecha final no puede ser anterior a la fecha inicial");
        }
    }

    private LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
}