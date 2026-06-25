package com.raccooncash.api.transaccion;

import com.raccooncash.api.cuenta.Cuenta;
import com.raccooncash.api.cuenta.CuentaRepositorio;
import com.raccooncash.api.categoria.Categoria;
import com.raccooncash.api.categoria.CategoriaRepositorio;
import com.raccooncash.api.categoria.TipoCategoria;
import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
import com.raccooncash.api.savinggoal.SavingGoal;
import com.raccooncash.api.savinggoal.SavingGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransaccionServicio {

    private final TransaccionRepositorio transactionRepository;
    private final CuentaRepositorio accountRepository;
    private final CategoriaRepositorio categoryRepository;
    private final SavingGoalRepository savingGoalRepository;

    public TransaccionServicio(TransaccionRepositorio transactionRepository,
                              CuentaRepositorio accountRepository,
                              CategoriaRepositorio categoryRepository,
                              SavingGoalRepository savingGoalRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.savingGoalRepository = savingGoalRepository;
    }

    @Transactional(readOnly = true)
    public List<TransaccionRespuesta> getTransactions(Long accountId,
                                                     Long categoryId,
                                                     TipoTransaccion type,
                                                     LocalDate from,
                                                     LocalDate to) {
        LocalDateTime fromDate = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDate = to != null ? to.atTime(LocalTime.MAX) : null;

        return transactionRepository.findWithFilters(accountId, categoryId, type, fromDate, toDate)
                .stream()
                .map(TransaccionRespuesta::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransaccionRespuesta getTransactionById(Long id) {
        Transaccion transaction = findActiveTransaction(id);
        return new TransaccionRespuesta(transaction);
    }

    @Transactional
    public TransaccionRespuesta createTransaction(TransaccionSolicitud request) {
        Transaccion transaction = buildTransactionFromRequest(new Transaccion(), request);
        applyTransactionEffect(transaction);

        saveAccountsFor(transaction);
        Transaccion savedTransaction = transactionRepository.save(transaction);
        return new TransaccionRespuesta(savedTransaction);
    }

    @Transactional
    public TransaccionRespuesta updateTransaction(Long id, TransaccionSolicitud request) {
        Transaccion transaction = findActiveTransaction(id);
        ensureTransactionCanBeManagedDirectly(transaction);

        revertTransactionEffect(transaction);
        saveAccountsFor(transaction);

        buildTransactionFromRequest(transaction, request);
        applyTransactionEffect(transaction);
        saveAccountsFor(transaction);

        Transaccion updatedTransaction = transactionRepository.save(transaction);
        return new TransaccionRespuesta(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaccion transaction = findActiveTransaction(id);
        ensureTransactionCanBeManagedDirectly(transaction);
        revertTransactionEffect(transaction);
        transaction.setActive(false);

        saveAccountsFor(transaction);
        transactionRepository.save(transaction);
    }

    private void ensureTransactionCanBeManagedDirectly(Transaccion transaction) {
        if (Boolean.TRUE.equals(transaction.getGeneratedByDebtPayment())) {
            throw new SolicitudIncorrectaException("Las transacciones generadas por pagos de deuda se gestionan desde el modulo de deudas");
        }
    }

    private Transaccion buildTransactionFromRequest(Transaccion transaction, TransaccionSolicitud request) {
        validateRequestBasics(request);

        Cuenta account = findActiveAccount(request.getAccountId(), "Cuenta no encontrada");
        Categoria category = null;
        Cuenta destinationAccount = null;
        SavingGoal savingGoal = null;

        if (request.getSavingGoalId() != null) {
            savingGoal = savingGoalRepository.findById(request.getSavingGoalId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("SavingGoal no encontrada"));
        } else if (request.getType() == TipoTransaccion.INCOME || request.getType() == TipoTransaccion.EXPENSE) {
            if (request.getCategoryId() == null) {
                throw new SolicitudIncorrectaException("La categoria es obligatoria para ingresos y gastos");
            }
            category = findActiveCategory(request.getCategoryId());
            validateCategoryType(request.getType(), category);
        }

        if (request.getType() == TipoTransaccion.TRANSFER && savingGoal == null) {
            Long destinationAccountId = request.getResolvedDestinationAccountId();
            if (destinationAccountId == null) {
                throw new SolicitudIncorrectaException("La cuenta destino es obligatoria para transferencias");
            }
            if (request.getAccountId().equals(destinationAccountId)) {
                throw new SolicitudIncorrectaException("La cuenta origen y destino no pueden ser la misma");
            }
            destinationAccount = findActiveAccount(destinationAccountId, "Cuenta destino no encontrada");
        }

        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate() != null ? request.getDate() : LocalDateTime.now());
        transaction.setType(request.getType());
        transaction.setAccount(account);
        transaction.setToAccount(destinationAccount);
        transaction.setCategory(category);
        transaction.setSavingGoal(savingGoal);
        transaction.setNotes(request.getNotes());
        transaction.setActive(true);

        return transaction;
    }

    private void validateRequestBasics(TransaccionSolicitud request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudIncorrectaException("El monto debe ser mayor a cero");
        }
        if (request.getType() == null) {
            throw new SolicitudIncorrectaException("El tipo de transaccion es obligatorio");
        }
        if (request.getAccountId() == null) {
            throw new SolicitudIncorrectaException("La cuenta es obligatoria");
        }
    }

    private void validateCategoryType(TipoTransaccion transactionType, Categoria category) {
        if (transactionType == TipoTransaccion.INCOME && category.getType() != TipoCategoria.INCOME) {
            throw new SolicitudIncorrectaException("La categoria debe ser de tipo INCOME");
        }
        if (transactionType == TipoTransaccion.EXPENSE && category.getType() != TipoCategoria.EXPENSE) {
            throw new SolicitudIncorrectaException("La categoria debe ser de tipo EXPENSE");
        }
    }

    private void applyTransactionEffect(Transaccion transaction) {
        if (transaction.getSavingGoal() != null) {
            ensureSufficientBalance(transaction.getAccount(), transaction.getAmount());
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.INCOME) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.EXPENSE) {
            ensureSufficientBalance(transaction.getAccount(), transaction.getAmount());
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.TRANSFER) {
            ensureSufficientBalance(transaction.getAccount(), transaction.getAmount());
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            transaction.getToAccount().setCurrentBalance(currentBalance(transaction.getToAccount()).add(transaction.getAmount()));
        }
    }

    private void revertTransactionEffect(Transaccion transaction) {
        if (transaction.getSavingGoal() != null) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.INCOME) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).subtract(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.EXPENSE) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            return;
        }

        if (transaction.getType() == TipoTransaccion.TRANSFER) {
            transaction.getAccount().setCurrentBalance(currentBalance(transaction.getAccount()).add(transaction.getAmount()));
            if (transaction.getToAccount() != null) {
                transaction.getToAccount().setCurrentBalance(currentBalance(transaction.getToAccount()).subtract(transaction.getAmount()));
            }
        }
    }

    private void ensureSufficientBalance(Cuenta account, BigDecimal amount) {
        if (currentBalance(account).compareTo(amount) < 0) {
            throw new SolicitudIncorrectaException("Saldo insuficiente en la cuenta " + account.getName());
        }
    }

    private BigDecimal currentBalance(Cuenta account) {
        return account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
    }

    private void saveAccountsFor(Transaccion transaction) {
        accountRepository.save(transaction.getAccount());
        if (transaction.getToAccount() != null) {
            accountRepository.save(transaction.getToAccount());
        }
    }

    private Transaccion findActiveTransaction(Long id) {
        return transactionRepository.findActiveById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transaccion no encontrada"));
    }

    private Cuenta findActiveAccount(Long id, String message) {
        return accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(message));
    }

    private Categoria findActiveCategory(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<TransaccionRespuesta> getTransactionsBySavingGoalId(Long savingGoalId) {
        SavingGoal savingGoal = savingGoalRepository.findById(savingGoalId)
                .orElseThrow(() -> new RecursoNoEncontradoException("SavingGoal no encontrada"));

        return transactionRepository.findBySavingGoalAndActiveTrue(savingGoal).stream()
                .map(TransaccionRespuesta::new)
                .collect(Collectors.toList());
    }
}
