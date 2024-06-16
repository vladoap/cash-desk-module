package com.fibank.cashdesk.controller;

import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.dto.CashOperationDTO;
import com.fibank.cashdesk.enums.CashOperationType;
import com.fibank.cashdesk.enums.Currency;
import com.fibank.cashdesk.service.FileUtilsService;
import com.fibank.cashdesk.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OperationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileUtilsService fileUtilsService;

    @Value("${api-key}")
    private String API_KEY;

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();


    @BeforeAll
    public static void setUpBeforeClass() throws IOException {
        createEmptyTestFile("src/test/resources/cash_balances_test.txt");
        createEmptyTestFile("src/test/resources/cash_operations_test.txt");
    }

    @AfterAll
    public static void tearDownAfterClass() {
        deleteTestFile("src/test/resources/cash_balances_test.txt");
        deleteTestFile("src/test/resources/cash_operations_test.txt");
    }



    @Test
    public void testCashOperationDeposit() throws Exception {
        CashBalanceDTO initialBalance = fileUtilsService.readLastBalance();

        CashOperationDTO cashOperationDTO = createCashOperationDTO(CashOperationType.DEPOSIT, BigDecimal.valueOf(50));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/cash-operation")
                        .header("FIB-X-AUTH", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(cashOperationDTO)))
                .andExpect(status().isOk());

        CashBalanceDTO updatedBalance = fileUtilsService.readLastBalance();

        assertUpdatedBalance(initialBalance, updatedBalance, cashOperationDTO);
    }

    @Test
    public void testCashOperationWithdraw() throws Exception {
        CashBalanceDTO initialBalance = fileUtilsService.readLastBalance();

        CashOperationDTO cashOperationDTO = createCashOperationDTO(CashOperationType.WITHDRAWAL, BigDecimal.valueOf(20));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/cash-operation")
                        .header("FIB-X-AUTH", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(cashOperationDTO)))
                .andExpect(status().isOk());

        CashBalanceDTO updatedBalance = fileUtilsService.readLastBalance();

        assertUpdatedBalance(initialBalance, updatedBalance, cashOperationDTO);
    }

    @Test
    public void testGetCashBalance() throws Exception {
        CashBalanceDTO lastBalance = fileUtilsService.readLastBalance();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/cash-balance")
                        .header("FIB-X-AUTH", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance.BGN.totalBalance").value(lastBalance.getBalance().get(Currency.BGN).getTotalBalance()))
                .andExpect(jsonPath("$.balance.BGN.denomination['10']").value(lastBalance.getBalance().get(Currency.BGN).getDenomination().get(10)))
                .andExpect(jsonPath("$.balance.BGN.denomination['50']").value(lastBalance.getBalance().get(Currency.BGN).getDenomination().get(50)))
                .andExpect(jsonPath("$.balance.EUR.totalBalance").value(lastBalance.getBalance().get(Currency.EUR).getTotalBalance()))
                .andExpect(jsonPath("$.balance.EUR.denomination['10']").value(lastBalance.getBalance().get(Currency.EUR).getDenomination().get(10)))
                .andExpect(jsonPath("$.balance.EUR.denomination['50']").value(lastBalance.getBalance().get(Currency.EUR).getDenomination().get(50)));
    }

    private CashOperationDTO createCashOperationDTO(CashOperationType operationType, BigDecimal amount) {
        CashOperationDTO cashOperationDTO = new CashOperationDTO();
        cashOperationDTO.setLocalDateTime(LocalDateTime.now());
        cashOperationDTO.setType(operationType);
        cashOperationDTO.setCurrency(Currency.BGN);
        cashOperationDTO.setAmount(amount);
        Map<Integer, Integer> denomination = new HashMap<>();
        denomination.put(10, amount.divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP).intValue());
        cashOperationDTO.setDenomination(denomination);
        return cashOperationDTO;
    }

    private void assertUpdatedBalance(CashBalanceDTO initialBalance, CashBalanceDTO updatedBalance, CashOperationDTO operation) {
        BigDecimal initialTotalBalance = initialBalance.getBalance().get(operation.getCurrency()).getTotalBalance();
        BigDecimal updatedTotalBalance = updatedBalance.getBalance().get(operation.getCurrency()).getTotalBalance();

        BigDecimal expectedTotalBalance;
        if (operation.getType() == CashOperationType.DEPOSIT) {
            expectedTotalBalance = initialTotalBalance.add(operation.getAmount());
        } else {
            expectedTotalBalance = initialTotalBalance.subtract(operation.getAmount());
        }

        assertEquals(expectedTotalBalance, updatedTotalBalance);

        Map<Integer, Integer> initialDenominations = initialBalance.getBalance().get(operation.getCurrency()).getDenomination();
        Map<Integer, Integer> updatedDenominations = updatedBalance.getBalance().get(operation.getCurrency()).getDenomination();
        for (Map.Entry<Integer, Integer> entry : operation.getDenomination().entrySet()) {
            int banknote = entry.getKey();
            int count = entry.getValue();
            int expectedCount = initialDenominations.getOrDefault(banknote, 0);
            if (operation.getType() == CashOperationType.DEPOSIT) {
                expectedCount += count;
            } else {
                expectedCount -= count;
            }
            assertEquals(expectedCount, updatedDenominations.get(banknote).intValue());
        }
    }

    private static void createEmptyTestFile(String filePath) throws IOException {
        File file = new File(filePath);
        Files.write(file.toPath(), new byte[0]);
    }

    private static void deleteTestFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

    }
}
