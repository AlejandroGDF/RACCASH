package com.raccooncash.api.deuda;

import com.raccooncash.api.categoria.Categoria;
import com.raccooncash.api.categoria.CategoriaRepositorio;
import com.raccooncash.api.categoria.TipoCategoria;
import com.raccooncash.api.cuenta.Cuenta;
import com.raccooncash.api.cuenta.CuentaRepositorio;
import com.raccooncash.api.excepcion.RecursoNoEncontradoException;
import com.raccooncash.api.excepcion.SolicitudIncorrectaException;
import com.raccooncash.api.transaccion.TipoTransaccion;
import com.raccooncash.api.transaccion.Transaccion;
import com.raccooncash.api.transaccion.TransaccionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class DeudaServicio {

    private final DeudaRepositorio debtRepository;
    private final PagoDeudaRepositorio paymentRepository;
    private final CuentaRepositorio accountRepository;
    private final TransaccionRepositorio transactionRepository;
    private final CategoriaRepositorio categoryRepository;

    public DeudaServicio(DeudaRepositorio debtRepository,
                          PagoDeudaRepositorio paymentRepository,
                          CuentaRepositorio accountRepository,
                          TransaccionRepositorio transactionRepository,
                          CategoriaRepositorio categoryRepository) {
        this.debtRepository = debtRepository;
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<DeudaRespuesta> getAllDebts(TipoDeuda type,
                                            EstadoDeuda status,
                                            Long accountId,
                                            LocalDate dueFrom,
                                            LocalDate dueTo,
                                            Boolean overdue,
                                            String search) {
        return debtRepository.findAllByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .filter(debt -> matchesType(debt, type))
                .filter(debt -> matchesStatus(debt, status))
                .filter(debt -> matchesAccount(debt, accountId))
                .filter(debt -> matchesDueRange(debt, dueFrom, dueTo))
                .filter(debt -> matchesOverdue(debt, overdue))
                .filter(debt -> matchesSearch(debt, search))
                .map(DeudaRespuesta::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeudaRespuesta getDebtById(Long id) {
        return new DeudaRespuesta(findActiveDebt(id));
    }

    @Transactional
    public DeudaRespuesta createDebt(DeudaSolicitud request) {
        validateDebtRequest(request);
        Cuenta account = findActiveAccount(request.getAccountId());

        Deuda debt = new Deuda();
        debt.setPersonName(request.getPersonName().trim());
        debt.setDescription(request.getDescription());
        debt.setTotalAmount(money(request.getTotalAmount()));
        debt.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        debt.setRemainingAmount(money(request.getTotalAmount()));
        debt.setType(request.getType());
        debt.setStatus(EstadoDeuda.PENDING);
        debt.setDueDate(request.getDueDate());
        debt.setAccount(account);
        debt.setActive(true);
        applyReminder(debt, request);

        Deuda savedDebt = debtRepository.save(debt);
        return new DeudaRespuesta(savedDebt);
    }

    @Transactional
    public DeudaRespuesta updateDebt(Long id, DeudaSolicitud request) {
        validateDebtRequest(request);
        Deuda debt = findActiveDebt(id);
        Cuenta account = findActiveAccount(request.getAccountId());

        BigDecimal paidAmount = money(safeAmount(debt.getPaidAmount()));
        if (request.getTotalAmount().compareTo(paidAmount) < 0) {
            throw new SolicitudIncorrectaException("El monto total no puede ser menor que lo ya pagado");
        }
        if (debt.getType() != request.getType() && paymentRepository.existsByDebtIdAndActiveTrue(id)) {
            throw new SolicitudIncorrectaException("No se puede cambiar el tipo de deuda porque ya tiene pagos registrados");
        }

        debt.setPersonName(request.getPersonName().trim());
        debt.setDescription(request.getDescription());
        debt.setTotalAmount(money(request.getTotalAmount()));
        debt.setPaidAmount(paidAmount);
        debt.setRemainingAmount(money(debt.getTotalAmount().subtract(paidAmount)));
        debt.setType(request.getType());
        debt.setDueDate(request.getDueDate());
        debt.setAccount(account);
        applyReminder(debt, request);
        updateStatus(debt);

        Deuda updatedDebt = debtRepository.save(debt);
        return new DeudaRespuesta(updatedDebt);
    }

    @Transactional
    public void deleteDebt(Long id) {
        Deuda debt = findActiveDebt(id);
        debt.setActive(false);
        debt.setStatus(EstadoDeuda.CANCELLED);
        debt.setReminderEnabled(false);
        debt.setReminderAt(null);
        debtRepository.save(debt);
    }

    @Transactional
    public PagoDeudaRespuesta addPayment(Long debtId, PagoDeudaSolicitud request) {
        validatePaymentRequest(request);
        Deuda debt = findActiveDebt(debtId);
        ensureDebtAllowsPayments(debt);

        Cuenta account = findActiveAccount(request.getAccountId());
        BigDecimal amount = money(request.getAmount());
        if (amount.compareTo(safeAmount(debt.getRemainingAmount())) > 0) {
            throw new SolicitudIncorrectaException("El pago no puede ser mayor que el monto pendiente");
        }

        LocalDate paymentDate = request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now();
        Transaccion transaction = buildPaymentTransaction(debt, account, amount, paymentDate, request.getNotes());
        applyPaymentEffect(debt, account, amount);
        accountRepository.save(account);
        Transaccion savedTransaction = transactionRepository.save(transaction);

        PagoDeuda payment = new PagoDeuda();
        payment.setDebt(debt);
        payment.setAccount(account);
        payment.setTransaction(savedTransaction);
        payment.setAmount(amount);
        payment.setPaymentDate(paymentDate);
        payment.setNotes(request.getNotes());
        payment.setActive(true);

        debt.setPaidAmount(money(safeAmount(debt.getPaidAmount()).add(amount)));
        debt.setRemainingAmount(money(debt.getTotalAmount().subtract(debt.getPaidAmount())));
        updateStatus(debt);
        debtRepository.save(debt);

        PagoDeuda savedPayment = paymentRepository.save(payment);
        return new PagoDeudaRespuesta(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PagoDeudaRespuesta> getPayments(Long debtId) {
        findActiveDebt(debtId);
        return paymentRepository.findAllByDebtIdAndActiveTrueOrderByPaymentDateDescCreatedAtDesc(debtId)
                .stream()
                .map(PagoDeudaRespuesta::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePayment(Long debtId, Long paymentId) {
        Deuda debt = findActiveDebt(debtId);
        PagoDeuda payment = paymentRepository.findByIdAndDebtIdAndActiveTrue(paymentId, debtId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago de deuda no encontrado"));

        Cuenta account = payment.getAccount() != null ? payment.getAccount() : debt.getAccount();
        reversePaymentEffect(payment, account);
        accountRepository.save(account);

        if (payment.getTransaction() != null) {
            payment.getTransaction().setActive(false);
            transactionRepository.save(payment.getTransaction());
        }
        payment.setActive(false);
        paymentRepository.save(payment);

        BigDecimal paidAmount = safeAmount(debt.getPaidAmount()).subtract(safeAmount(payment.getAmount()));
        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            paidAmount = BigDecimal.ZERO;
        }
        debt.setPaidAmount(money(paidAmount));
        debt.setRemainingAmount(money(debt.getTotalAmount().subtract(debt.getPaidAmount())));
        updateStatus(debt);
        debtRepository.save(debt);
    }

    @Transactional(readOnly = true)
    public List<DeudaRespuesta> getPendingReminders() {
        LocalDateTime now = LocalDateTime.now();
        return debtRepository.findAllByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .filter(debt -> Boolean.TRUE.equals(debt.getReminderEnabled()))
                .filter(debt -> debt.getReminderAt() != null && !debt.getReminderAt().isAfter(now))
                .filter(debt -> debt.getStatus() != EstadoDeuda.PAID && debt.getStatus() != EstadoDeuda.CANCELLED)
                .sorted(Comparator.comparing(Deuda::getReminderAt))
                .map(DeudaRespuesta::new)
                .collect(Collectors.toList());
    }

    private Transaccion buildPaymentTransaction(Deuda debt,
                                                Cuenta account,
                                                BigDecimal amount,
                                                LocalDate paymentDate,
                                                String notes) {
        Transaccion transaction = new Transaccion();
        transaction.setDescription(paymentDescription(debt));
        transaction.setAmount(amount);
        transaction.setDate(paymentDate.atStartOfDay());
        transaction.setType(debt.getType() == TipoDeuda.I_OWE ? TipoTransaccion.EXPENSE : TipoTransaccion.INCOME);
        transaction.setAccount(account);
        transaction.setCategory(resolvePaymentCategory(debt));
        transaction.setNotes(notes);
        transaction.setActive(true);
        transaction.setGeneratedByDebtPayment(true);
        return transaction;
    }

    private Categoria resolvePaymentCategory(Deuda debt) {
        if (debt.getType() == TipoDeuda.I_OWE) {
            return findOrCreateCategory("Deudas", TipoCategoria.EXPENSE, "#b91c1c", "credit-card");
        }
        return findOrCreateCategory("Pagos", TipoCategoria.INCOME, "#22c55e", "hand-coins");
    }

    private Categoria findOrCreateCategory(String name, TipoCategoria type, String color, String icon) {
        return categoryRepository.findFirstByNameIgnoreCaseAndTypeAndActiveTrue(name, type)
                .orElseGet(() -> {
                    Categoria category = new Categoria();
                    category.setName(name);
                    category.setType(type);
                    category.setColor(color);
                    category.setIcon(icon);
                    category.setActive(true);
                    return categoryRepository.save(category);
                });
    }

    private String paymentDescription(Deuda debt) {
        if (debt.getType() == TipoDeuda.I_OWE) {
            return "Pago de deuda a " + debt.getPersonName();
        }
        return "Cobro de prestamo de " + debt.getPersonName();
    }

    private void applyPaymentEffect(Deuda debt, Cuenta account, BigDecimal amount) {
        if (debt.getType() == TipoDeuda.I_OWE) {
            ensureSufficientBalance(account, amount);
            account.setCurrentBalance(money(currentBalance(account).subtract(amount)));
            return;
        }

        account.setCurrentBalance(money(currentBalance(account).add(amount)));
    }

    private void reversePaymentEffect(PagoDeuda payment, Cuenta account) {
        BigDecimal amount = safeAmount(payment.getAmount());
        TipoTransaccion transactionType = payment.getTransaction() != null ? payment.getTransaction().getType() : null;

        if (transactionType == TipoTransaccion.EXPENSE || payment.getDebt().getType() == TipoDeuda.I_OWE) {
            account.setCurrentBalance(money(currentBalance(account).add(amount)));
            return;
        }

        account.setCurrentBalance(money(currentBalance(account).subtract(amount)));
    }

    private void ensureDebtAllowsPayments(Deuda debt) {
        if (debt.getStatus() == EstadoDeuda.PAID || debt.getStatus() == EstadoDeuda.CANCELLED) {
            throw new SolicitudIncorrectaException("No se pueden registrar pagos en deudas pagadas o canceladas");
        }
    }

    private void updateStatus(Deuda debt) {
        if (debt.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus(EstadoDeuda.PAID);
            return;
        }

        if (debt.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            debt.setStatus(EstadoDeuda.PARTIALLY_PAID);
            return;
        }

        debt.setStatus(EstadoDeuda.PENDING);
    }

    private void validateDebtRequest(DeudaSolicitud request) {
        if (request.getPersonName() == null || request.getPersonName().isBlank()) {
            throw new SolicitudIncorrectaException("El nombre de la persona es obligatorio");
        }
        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudIncorrectaException("El monto total debe ser mayor a cero");
        }
        if (request.getType() == null) {
            throw new SolicitudIncorrectaException("El tipo de deuda es obligatorio");
        }
        if (request.getAccountId() == null) {
            throw new SolicitudIncorrectaException("La cuenta es obligatoria");
        }
        if (Boolean.TRUE.equals(request.getReminderEnabled()) && request.getReminderAt() == null) {
            throw new SolicitudIncorrectaException("La fecha del recordatorio es obligatoria si el recordatorio esta habilitado");
        }
        if (Boolean.TRUE.equals(request.getReminderEnabled())
                && request.getReminderAt() != null
                && request.getDueDate() != null
                && request.getReminderAt().toLocalDate().isAfter(request.getDueDate())) {
            throw new SolicitudIncorrectaException("El recordatorio no puede ser posterior a la fecha limite");
        }
    }

    private void validatePaymentRequest(PagoDeudaSolicitud request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudIncorrectaException("El pago debe ser mayor a cero");
        }
        if (request.getAccountId() == null) {
            throw new SolicitudIncorrectaException("La cuenta es obligatoria");
        }
    }

    private void applyReminder(Deuda debt, DeudaSolicitud request) {
        boolean reminderEnabled = Boolean.TRUE.equals(request.getReminderEnabled());
        debt.setReminderEnabled(reminderEnabled);
        debt.setReminderAt(reminderEnabled ? request.getReminderAt() : null);
    }

    private boolean matchesType(Deuda debt, TipoDeuda type) {
        return type == null || debt.getType() == type;
    }

    private boolean matchesStatus(Deuda debt, EstadoDeuda status) {
        return status == null || debt.getStatus() == status;
    }

    private boolean matchesAccount(Deuda debt, Long accountId) {
        return accountId == null || debt.getAccount().getId().equals(accountId);
    }

    private boolean matchesDueRange(Deuda debt, LocalDate dueFrom, LocalDate dueTo) {
        LocalDate dueDate = debt.getDueDate();
        if (dueFrom != null && (dueDate == null || dueDate.isBefore(dueFrom))) {
            return false;
        }
        return dueTo == null || (dueDate != null && !dueDate.isAfter(dueTo));
    }

    private boolean matchesOverdue(Deuda debt, Boolean overdue) {
        return overdue == null || overdue.equals(isOverdue(debt));
    }

    private boolean matchesSearch(Deuda debt, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String term = search.toLowerCase(Locale.ROOT);
        return containsTerm(debt.getPersonName(), term) || containsTerm(debt.getDescription(), term);
    }

    private boolean containsTerm(String value, String term) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(term);
    }

    private boolean isOverdue(Deuda debt) {
        return debt.getDueDate() != null
                && debt.getDueDate().isBefore(LocalDate.now())
                && debt.getStatus() != EstadoDeuda.PAID
                && debt.getStatus() != EstadoDeuda.CANCELLED;
    }

    private Deuda findActiveDebt(Long id) {
        return debtRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Deuda no encontrada"));
    }

    private Cuenta findActiveAccount(Long id) {
        return accountRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada"));
    }

    private void ensureSufficientBalance(Cuenta account, BigDecimal amount) {
        if (currentBalance(account).compareTo(amount) < 0) {
            throw new SolicitudIncorrectaException("Saldo insuficiente en la cuenta " + account.getName());
        }
    }

    private BigDecimal currentBalance(Cuenta account) {
        return safeAmount(account.getCurrentBalance());
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private BigDecimal money(BigDecimal amount) {
        return safeAmount(amount).setScale(2, RoundingMode.HALF_UP);
    }
}
