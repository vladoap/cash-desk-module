package com.fibank.cashdesk.service;

import com.fibank.cashdesk.dto.CashBalanceCurrencyDTO;
import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.dto.CashOperationDTO;
import com.fibank.cashdesk.enums.CashOperationType;
import com.fibank.cashdesk.enums.Currency;
import com.fibank.cashdesk.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class FileUtilsServiceTest {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    @TempDir
    Path tempFolder;

    private File tempCashOperationsFile;
    private File tempCashBalancesFile;

    private FileUtilsService fileUtilsService;

    @BeforeEach
    public void setUp() {
        tempCashOperationsFile = new File(tempFolder.toFile(), "cash_operations_test.txt");
        tempCashBalancesFile = new File(tempFolder.toFile(), "cash_balances_test.txt");

        fileUtilsService = new FileUtilsService(
                tempCashBalancesFile.getPath(),
                tempCashOperationsFile.getPath()
        );
    }

    @Test
    public void testWriteCashOperation() throws IOException {
        CashOperationDTO cashOperationDTO = new CashOperationDTO();
        cashOperationDTO.setLocalDateTime(LocalDateTime.now());
        cashOperationDTO.setType(CashOperationType.DEPOSIT);
        cashOperationDTO.setCurrency(Currency.BGN);
        cashOperationDTO.setAmount(BigDecimal.valueOf(100));
        Map<Integer, Integer> denomination = new HashMap<>();
        denomination.put(10, 5);
        cashOperationDTO.setDenomination(denomination);

        fileUtilsService.writeCashOperation(cashOperationDTO);

        assertTrue(tempCashOperationsFile.exists());

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempCashOperationsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String fileContent = sb.toString();
        CashOperationDTO result = gson.fromJson(fileContent, CashOperationDTO.class);

        assertNotNull(result);
        assertEquals(cashOperationDTO.getLocalDateTime(), result.getLocalDateTime());
        assertEquals(cashOperationDTO.getType(), result.getType());
        assertEquals(cashOperationDTO.getCurrency(), result.getCurrency());
        assertEquals(cashOperationDTO.getAmount(), result.getAmount());
        assertEquals(cashOperationDTO.getDenomination(), result.getDenomination());
    }

    @Test
    public void testWriteCashBalance() throws IOException {
        CashBalanceDTO cashBalanceDTO = createCashBalanceDTO();

        fileUtilsService.writeCashBalance(cashBalanceDTO);

        assertTrue(tempCashBalancesFile.exists());

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(tempCashBalancesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String fileContent = sb.toString();

        CashBalanceDTO result = gson.fromJson(fileContent, CashBalanceDTO.class);

        assertNotNull(result);
        assertEquals(cashBalanceDTO.getLocalDateTime(), result.getLocalDateTime());
        assertEquals(cashBalanceDTO.getBalance().get(Currency.BGN).getTotalBalance(), result.getBalance().get(Currency.BGN).getTotalBalance());
        assertEquals(cashBalanceDTO.getBalance().get(Currency.BGN).getDenomination(), result.getBalance().get(Currency.BGN).getDenomination());
        assertEquals(cashBalanceDTO.getBalance().get(Currency.EUR).getTotalBalance(), result.getBalance().get(Currency.EUR).getTotalBalance());
        assertEquals(cashBalanceDTO.getBalance().get(Currency.EUR).getDenomination(), result.getBalance().get(Currency.EUR).getDenomination());
    }

    @Test
    public void testReadLastBalance() throws IOException {
        CashBalanceDTO cashBalanceDTO = createCashBalanceDTO();

        fileUtilsService.writeCashBalance(cashBalanceDTO);

        CashBalanceDTO actualBalance = fileUtilsService.readLastBalance();

        assertNotNull(actualBalance);
        assertEquals(cashBalanceDTO.getLocalDateTime(), actualBalance.getLocalDateTime());
        assertEquals(cashBalanceDTO.getBalance().get(Currency.BGN).getTotalBalance(), actualBalance.getBalance().get(Currency.BGN).getTotalBalance());
        assertEquals(cashBalanceDTO.getBalance().get(Currency.BGN).getDenomination(), actualBalance.getBalance().get(Currency.BGN).getDenomination());
        assertEquals(cashBalanceDTO.getBalance().get(Currency.EUR).getTotalBalance(), actualBalance.getBalance().get(Currency.EUR).getTotalBalance());
        assertEquals(cashBalanceDTO.getBalance().get(Currency.EUR).getDenomination(), actualBalance.getBalance().get(Currency.EUR).getDenomination());
    }


    @Test
    public void testIsCashBalanceFileEmpty() throws IOException {
        Files.write(tempCashBalancesFile.toPath(), new byte[0]);
        CashBalanceDTO cashBalanceDTO = createCashBalanceDTO();
        assertTrue(fileUtilsService.isCashBalanceFileEmpty());
        fileUtilsService.writeCashBalance(cashBalanceDTO);
        assertFalse(fileUtilsService.isCashBalanceFileEmpty());
    }



    private static CashBalanceDTO createCashBalanceDTO() {
        CashBalanceDTO cashBalanceDTO = new CashBalanceDTO();
        cashBalanceDTO.setLocalDateTime(LocalDateTime.now());

        Map<Currency, CashBalanceCurrencyDTO> balance = new HashMap<>();
        CashBalanceCurrencyDTO bgnBalance = new CashBalanceCurrencyDTO();
        bgnBalance.setTotalBalance(BigDecimal.valueOf(1000));
        Map<Integer, Integer> bgnDenomination = new HashMap<>();
        bgnDenomination.put(10, 100);
        bgnDenomination.put(50, 20);
        bgnBalance.setDenomination(bgnDenomination);
        balance.put(Currency.BGN, bgnBalance);

        CashBalanceCurrencyDTO eurBalance = new CashBalanceCurrencyDTO();
        eurBalance.setTotalBalance(BigDecimal.valueOf(2000));
        Map<Integer, Integer> eurDenomination = new HashMap<>();
        eurDenomination.put(10, 50);
        eurDenomination.put(20, 30);
        eurBalance.setDenomination(eurDenomination);
        balance.put(Currency.EUR, eurBalance);

        cashBalanceDTO.setBalance(balance);
        return cashBalanceDTO;
    }
}