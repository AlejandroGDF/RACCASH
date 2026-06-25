package com.raccooncash.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:raccoon_cash_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "raccoon-cash.data.seed-default-categories=false"
})
class AplicacionRaccoonCashApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void getTransactionsReturnsEmptyListWhenThereAreNoTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getTransactionsReturnsSameFlatContractAsCreateTransaction() throws Exception {
        Long accountId = createAccount();
        Long categoryId = createCategory();

        String transactionRequest = """
                {
                  "description": "Pago salario",
                  "amount": 12000,
                  "type": "INCOME",
                  "date": "2026-06-03T10:00:00",
                  "accountId": %d,
                  "categoryId": %d,
                  "notes": "Ingreso mensual"
                }
                """.formatted(accountId, categoryId);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account").doesNotExist())
                .andExpect(jsonPath("$.category").doesNotExist())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.accountName").value("Efectivo"))
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.categoryName").value("Salario"));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].account").doesNotExist())
                .andExpect(jsonPath("$[0].category").doesNotExist())
                .andExpect(jsonPath("$[0].accountId").value(accountId))
                .andExpect(jsonPath("$[0].accountName").value("Efectivo"))
                .andExpect(jsonPath("$[0].categoryId").value(categoryId))
                .andExpect(jsonPath("$[0].categoryName").value("Salario"));
    }

    @Test
    void createAndUpdateCategoryPersistParentCategoryId() throws Exception {
        Long parentId = createCategory("Comida", "EXPENSE");

        MvcResult childResult = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Samantha",
                                  "type": "EXPENSE",
                                  "color": "#7E57C2",
                                  "icon": "flower",
                                  "parentCategoryId": %d
                                }
                                """.formatted(parentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentCategoryId").value(parentId))
                .andReturn();

        Number childIdNumber = JsonPath.read(childResult.getResponse().getContentAsString(), "$.id");
        Long childId = childIdNumber.longValue();

        MvcResult categoriesResult = mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andReturn();

        List<Number> parentIds = JsonPath.read(
                categoriesResult.getResponse().getContentAsString(),
                "$[?(@.id == " + childId + ")].parentCategoryId");
        assertThat(parentIds).hasSize(1);
        assertThat(parentIds.get(0).longValue()).isEqualTo(parentId);

        Long updatedParentId = createCategory("Servicios", "EXPENSE");

        mockMvc.perform(put("/api/categories/{id}", childId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Samantha",
                                  "type": "EXPENSE",
                                  "color": "#7E57C2",
                                  "icon": "flower",
                                  "parentCategoryId": %d
                                }
                                """.formatted(updatedParentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentCategoryId").value(updatedParentId));
    }

    @Test
    void deleteBudgetThroughSpanishEndpointRemovesItFromActiveList() throws Exception {
        MvcResult createdBudget = mockMvc.perform(post("/api/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Presupuesto diario",
                                  "monto": 10000,
                                  "tipoPeriodo": "DIARIO",
                                  "valorPeriodo": 1,
                                  "fechaInicio": "2026-06-22",
                                  "color": "#22C55E",
                                  "esGasto": true,
                                  "incluirTodasLasTransacciones": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Presupuesto diario"))
                .andExpect(jsonPath("$.tipoPeriodo").value("DIARIO"))
                .andExpect(jsonPath("$.fechaFin").value("2026-06-22"))
                .andExpect(jsonPath("$.montoActual").value(0))
                .andReturn();

        Number budgetId = JsonPath.read(createdBudget.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(put("/api/presupuestos/{id}", budgetId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Presupuesto diario actualizado",
                                  "monto": 15000,
                                  "tipoPeriodo": "DIARIO",
                                  "valorPeriodo": 2,
                                  "fechaInicio": "2026-06-22",
                                  "color": "#3B82F6",
                                  "esGasto": true,
                                  "incluirTodasLasTransacciones": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Presupuesto diario actualizado"))
                .andExpect(jsonPath("$.tipoPeriodo").value("DIARIO"))
                .andExpect(jsonPath("$.fechaFin").value("2026-06-23"));

        mockMvc.perform(delete("/api/presupuestos/{id}", budgetId.longValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/presupuestos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void savingBudgetCanTrackIncomeCategoryLimits() throws Exception {
        Long accountId = createAccount();
        Long categoryId = createCategory("Ahorro", "INCOME");

        MvcResult createdBudget = mockMvc.perform(post("/api/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Presupuesto de ahorro",
                                  "monto": 1000,
                                  "tipoPeriodo": "MENSUAL",
                                  "valorPeriodo": 1,
                                  "fechaInicio": "2026-06-01",
                                  "color": "#22C55E",
                                  "esGasto": false,
                                  "incluirTodasLasTransacciones": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esGasto").value(false))
                .andReturn();

        Number budgetId = JsonPath.read(createdBudget.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/presupuestos/{id}/categories", budgetId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "amountLimit": 1000
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(categoryId));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Aporte de ahorro",
                                  "amount": 300,
                                  "type": "INCOME",
                                  "date": "2026-06-15T10:00:00",
                                  "accountId": %d,
                                  "categoryId": %d
                                }
                                """.formatted(accountId, categoryId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/presupuestos/{id}", budgetId.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoActual").value(300));
    }

    @Test
    void savingGoalTransactionsAcceptTransferWithoutDestinationAccount() throws Exception {
        Long accountId = createAccount();
        Long goalId = createSavingGoal();

        mockMvc.perform(get("/api/saving-goals/{id}/transactions", goalId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Ahorro para Emergencias",
                                  "amount": 250,
                                  "type": "TRANSFER",
                                  "date": "2026-06-24T10:00:00",
                                  "accountId": %d,
                                  "savingGoalId": %d
                                }
                                """.formatted(accountId, goalId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savingGoalId").value(goalId))
                .andExpect(jsonPath("$.type").value("TRANSFER"));

        mockMvc.perform(get("/api/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(4750));

        mockMvc.perform(get("/api/saving-goals/{id}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(250))
                .andExpect(jsonPath("$.transactionCount").value(1));

        mockMvc.perform(get("/api/saving-goals/{id}/transactions", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Ahorro para Emergencias"));
    }

    @Test
    void createDebtsDoesNotChangeAccountBalance() throws Exception {
        Long accountId = createAccount();

        mockMvc.perform(post("/api/debts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personName": "Juan",
                                  "description": "Prestamo personal",
                                  "totalAmount": 500,
                                  "type": "I_OWE",
                                  "dueDate": "2026-06-30",
                                  "accountId": %d,
                                  "reminderEnabled": true,
                                  "reminderAt": "2026-06-29T09:00:00"
                                }
                                """.formatted(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidAmount").value(0))
                .andExpect(jsonPath("$.remainingAmount").value(500))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.overdue").value(false))
                .andExpect(jsonPath("$.reminderEnabled").value(true))
                .andExpect(jsonPath("$.reminderAt").value("2026-06-29T09:00:00"));

        mockMvc.perform(post("/api/debts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personName": "Carlos",
                                  "description": "Me debe dinero",
                                  "totalAmount": 300,
                                  "type": "OWED_TO_ME",
                                  "dueDate": "2026-07-05",
                                  "accountId": %d
                                }
                                """.formatted(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/api/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(5000));
    }

    @Test
    void partialPaymentForIOweSubtractsBalanceCreatesExpenseAndBlocksDirectTransactionDelete() throws Exception {
        Long accountId = createAccount();
        Long debtId = createDebt(accountId, "Juan", "I_OWE", 500, "2026-06-30");

        MvcResult paymentResult = mockMvc.perform(post("/api/debts/{id}/payments", debtId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 200,
                                  "paymentDate": "2026-06-25",
                                  "accountId": %d,
                                  "notes": "Abono desde efectivo"
                                }
                                """.formatted(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debtId").value(debtId))
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        Number transactionId = JsonPath.read(paymentResult.getResponse().getContentAsString(), "$.transactionId");

        mockMvc.perform(get("/api/debts/{id}", debtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidAmount").value(200))
                .andExpect(jsonPath("$.remainingAmount").value(300))
                .andExpect(jsonPath("$.status").value("PARTIALLY_PAID"));

        mockMvc.perform(get("/api/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(4800));

        mockMvc.perform(get("/api/transactions")
                        .param("accountId", accountId.toString())
                        .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(transactionId.longValue()))
                .andExpect(jsonPath("$[0].description").value("Pago de deuda a Juan"))
                .andExpect(jsonPath("$[0].categoryName").value("Deudas"))
                .andExpect(jsonPath("$[0].generatedByDebtPayment").value(true));

        mockMvc.perform(delete("/api/transactions/{id}", transactionId.longValue()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completePaymentForIOweMarksDebtAsPaidAndRejectsFurtherPayments() throws Exception {
        Long accountId = createAccount();
        Long debtId = createDebt(accountId, "Juan", "I_OWE", 300, "2026-06-30");

        mockMvc.perform(post("/api/debts/{id}/payments", debtId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 300,
                                  "paymentDate": "2026-06-25",
                                  "accountId": %d
                                }
                                """.formatted(accountId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/debts/{id}", debtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidAmount").value(300))
                .andExpect(jsonPath("$.remainingAmount").value(0))
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(post("/api/debts/{id}/payments", debtId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 1,
                                  "accountId": %d
                                }
                                """.formatted(accountId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paymentForOwedToMeAddsBalanceAndCreatesIncome() throws Exception {
        Long accountId = createAccount();
        Long debtId = createDebt(accountId, "Carlos", "OWED_TO_ME", 300, "2026-07-05");

        mockMvc.perform(post("/api/debts/{id}/payments", debtId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 300,
                                  "paymentDate": "2026-06-25",
                                  "accountId": %d
                                }
                                """.formatted(accountId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(5300));

        mockMvc.perform(get("/api/debts/{id}", debtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/api/transactions")
                        .param("accountId", accountId.toString())
                        .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Cobro de prestamo de Carlos"))
                .andExpect(jsonPath("$[0].categoryName").value("Pagos"))
                .andExpect(jsonPath("$[0].generatedByDebtPayment").value(true));
    }

    @Test
    void invalidDebtPaymentsReturnBadRequest() throws Exception {
        Long accountId = createAccount();
        Long debtId = createDebt(accountId, "Juan", "I_OWE", 100, "2026-06-30");

        mockMvc.perform(post("/api/debts/{id}/payments", debtId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 101,
                                  "accountId": %d
                                }
                                """.formatted(accountId)))
                .andExpect(status().isBadRequest());

        Long lowBalanceAccountId = createAccount("Caja chica", 50);
        Long expensiveDebtId = createDebt(lowBalanceAccountId, "Ana", "I_OWE", 100, "2026-06-30");

        mockMvc.perform(post("/api/debts/{id}/payments", expensiveDebtId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 80,
                                  "accountId": %d
                                }
                                """.formatted(lowBalanceAccountId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePaymentRevertsBalanceCancelsTransactionAndRecalculatesDebt() throws Exception {
        Long accountId = createAccount();
        Long debtId = createDebt(accountId, "Juan", "I_OWE", 500, "2026-06-30");

        MvcResult paymentResult = mockMvc.perform(post("/api/debts/{id}/payments", debtId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 200,
                                  "paymentDate": "2026-06-25",
                                  "accountId": %d
                                }
                                """.formatted(accountId)))
                .andExpect(status().isOk())
                .andReturn();

        Number paymentId = JsonPath.read(paymentResult.getResponse().getContentAsString(), "$.id");
        Number transactionId = JsonPath.read(paymentResult.getResponse().getContentAsString(), "$.transactionId");

        mockMvc.perform(delete("/api/debts/{id}/payments/{paymentId}", debtId, paymentId.longValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(5000));

        mockMvc.perform(get("/api/debts/{id}", debtId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidAmount").value(0))
                .andExpect(jsonPath("$.remainingAmount").value(500))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/api/debts/{id}/payments", debtId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mockMvc.perform(get("/api/transactions/{id}", transactionId.longValue()))
                .andExpect(status().isNotFound());
    }

    @Test
    void debtFiltersAndPendingRemindersWork() throws Exception {
        Long cashAccountId = createAccount();
        Long bankAccountId = createAccount("Banco", 7000);

        Long overdueDebtId = createDebtWithReminder(cashAccountId, "Luis", "I_OWE", 150, "2020-01-02", "2020-01-01T09:00:00");
        createDebt(bankAccountId, "Carlos", "OWED_TO_ME", 300, "2030-07-05");

        mockMvc.perform(get("/api/debts")
                        .param("type", "I_OWE")
                        .param("overdue", "true")
                        .param("search", "luis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(overdueDebtId))
                .andExpect(jsonPath("$[0].overdue").value(true));

        mockMvc.perform(get("/api/debts")
                        .param("accountId", bankAccountId.toString())
                        .param("dueFrom", "2030-01-01")
                        .param("dueTo", "2030-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].personName").value("Carlos"));

        mockMvc.perform(get("/api/debts/reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(overdueDebtId))
                .andExpect(jsonPath("$[0].reminderEnabled").value(true));
    }

    private Long createAccount() throws Exception {
        return createAccount("Efectivo", 5000);
    }

    private Long createAccount(String name, int initialBalance) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "CASH",
                                  "initialBalance": %d,
                                  "currency": "C$",
                                  "color": "#4CAF50"
                                }
                                """.formatted(name, initialBalance)))
                .andExpect(status().isOk())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long createCategory() throws Exception {
        return createCategory("Salario", "INCOME");
    }

    private Long createCategory(String name, String type) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "%s",
                                  "color": "#00BCD4",
                                  "icon": "work"
                                }
                                """.formatted(name, type)))
                .andExpect(status().isOk())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long createSavingGoal() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/saving-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Emergencias",
                                  "targetAmount": 1000,
                                  "deadline": "2026-12-31",
                                  "color": "#22C55E",
                                  "icon": "💰",
                                  "currency": "C$"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long createDebt(Long accountId, String personName, String type, int totalAmount, String dueDate) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/debts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personName": "%s",
                                  "description": "Deuda de prueba",
                                  "totalAmount": %d,
                                  "type": "%s",
                                  "dueDate": "%s",
                                  "accountId": %d
                                }
                                """.formatted(personName, totalAmount, type, dueDate, accountId)))
                .andExpect(status().isOk())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private Long createDebtWithReminder(Long accountId,
                                        String personName,
                                        String type,
                                        int totalAmount,
                                        String dueDate,
                                        String reminderAt) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/debts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personName": "%s",
                                  "description": "Deuda con recordatorio",
                                  "totalAmount": %d,
                                  "type": "%s",
                                  "dueDate": "%s",
                                  "accountId": %d,
                                  "reminderEnabled": true,
                                  "reminderAt": "%s"
                                }
                                """.formatted(personName, totalAmount, type, dueDate, accountId, reminderAt)))
                .andExpect(status().isOk())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
