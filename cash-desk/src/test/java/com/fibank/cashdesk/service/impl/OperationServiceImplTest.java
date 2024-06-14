//package com.fibank.cashdesk.service.impl;
//
//import com.fibank.cashdesk.config.BanknotesDenominationsConfig;
//import com.fibank.cashdesk.dto.CashBalanceDTO;
//import com.fibank.cashdesk.dto.CashOperationDTO;
//import com.fibank.cashdesk.entity.CashBalance;
//import com.fibank.cashdesk.entity.CashBalanceCurrency;
//import com.fibank.cashdesk.enums.CashOperationType;
//import com.fibank.cashdesk.enums.Currency;
//import com.fibank.cashdesk.exception.InvalidBanknoteDenominationException;
//import com.fibank.cashdesk.exception.InvalidBanknoteQuantityException;
//import com.fibank.cashdesk.mapper.CashBalanceMapper;
//import com.fibank.cashdesk.utils.FileUtilsHelper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.math.BigDecimal;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OperationServiceImplTest {
//
//    @Mock
//    private CashBalanceMapper cashBalanceMapper;
//
//    @Mock
//    private BanknotesDenominationsConfig denominations;
//
//    @Mock
//    private FileUtilsHelper fileUtilsHelper;
//
//    @InjectMocks
//    private OperationServiceImpl operationService;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        denominations = new BanknotesDenominationsConfig();
//        Map<String, Set<Integer>> denominationMap = new HashMap<>();
//
//        Set<Integer> bgnDenominations = new HashSet<>();
//        Collections.addAll(bgnDenominations, 5, 10, 20, 50, 100);
//        denominationMap.put("BGN", bgnDenominations);
//
//        Set<Integer> eurDenominations = new HashSet<>();
//        Collections.addAll(eurDenominations, 5, 10, 20, 50, 100, 200, 500);
//        denominationMap.put("EUR", eurDenominations);
//
//        denominations.setDenominations(denominationMap);
//
//        Field apiKeyField = OperationServiceImpl.class.getDeclaredField("apiKey");
//        apiKeyField.setAccessible(true);
//        apiKeyField.set(operationService, "test-api-key");
//
//        Field denominationsField = OperationServiceImpl.class.getDeclaredField("denominations");
//        denominationsField.setAccessible(true);
//        denominationsField.set(operationService, denominations);
//    }
//
//    @Test
//    void testIsApiKeyValid() {
//        assertTrue(operationService.isApiKeyValid("test-api-key"));
//        assertFalse(operationService.isApiKeyValid("invalid-api-key"));
//    }
//
//    @Test
//    void testRetrieveCashBalance() throws IOException {
//        CashBalanceDTO cashBalanceDTO = new CashBalanceDTO();
//        CashBalance cashBalance = new CashBalance();
//
//        when(fileUtilsHelper.readLastBalance()).thenReturn(cashBalanceDTO);
//        when(cashBalanceMapper.toCashBalance(cashBalanceDTO)).thenReturn(cashBalance);
//
//        CashBalanceDTO result = operationService.retrieveCashBalance();
//        assertEquals(cashBalanceDTO, result);
//    }
//
//    // Normal Execution Tests
//
//    @Test
//    void testDepositOperationUpdatesBalance() throws IOException {
//        CashOperationDTO cashOperationDTO = new CashOperationDTO();
//        cashOperationDTO.setType(CashOperationType.DEPOSIT);
//        cashOperationDTO.setCurrency(Currency.BGN);
//        Map<Integer, Integer> operationDenominations = new HashMap<>();
//        operationDenominations.put(10, 10);
//        cashOperationDTO.setDenomination(operationDenominations);
//
//        CashBalanceDTO cashBalanceDTO = new CashBalanceDTO();
//        CashBalance cashBalance = new CashBalance();
//        CashBalanceCurrency cashBalanceCurrency = new CashBalanceCurrency();
//        cashBalanceCurrency.setDenomination(new HashMap<>());
//        cashBalanceCurrency.setTotalBalance(BigDecimal.ZERO);
//        cashBalance.getBalance().put(Currency.BGN, cashBalanceCurrency);
//
//        when(fileUtilsHelper.readLastBalance()).thenReturn(cashBalanceDTO);
//        when(cashBalanceMapper.toCashBalance(cashBalanceDTO)).thenReturn(cashBalance);
//        when(cashBalanceMapper.toCashBalanceDTO(any(CashBalance.class))).thenReturn(cashBalanceDTO);
//
//        CashOperationDTO result = operationService.performCashOperation(cashOperationDTO);
//        assertEquals(cashOperationDTO, result);
//        assertEquals(BigDecimal.valueOf(100), cashBalanceCurrency.getTotalBalance());
//        assertEquals(10, (int) cashBalanceCurrency.getDenomination().get(10));
//
//        verify(fileUtilsHelper, times(1)).writeCashBalance(any(CashBalanceDTO.class));
//        verify(fileUtilsHelper, times(1)).writeCashOperation(any(CashOperationDTO.class));
//    }
//
//    @Test
//    void testWithdrawalOperationUpdatesBalance() throws IOException {
//        CashOperationDTO cashOperationDTO = new CashOperationDTO();
//        cashOperationDTO.setType(CashOperationType.WITHDRAWAL);
//        cashOperationDTO.setCurrency(Currency.BGN);
//        cashOperationDTO.setAmount(BigDecimal.valueOf(100));  // Set the amount field
//        Map<Integer, Integer> operationDenominations = new HashMap<>();
//        operationDenominations.put(10, 10);
//        cashOperationDTO.setDenomination(operationDenominations);
//
//        CashBalanceDTO cashBalanceDTO = new CashBalanceDTO();
//        CashBalance cashBalance = new CashBalance();
//        CashBalanceCurrency cashBalanceCurrency = new CashBalanceCurrency();
//        Map<Integer, Integer> initialDenominations = new HashMap<>();
//        initialDenominations.put(10, 20);
//        cashBalanceCurrency.setDenomination(initialDenominations);
//        cashBalanceCurrency.setTotalBalance(BigDecimal.valueOf(200));
//        cashBalance.getBalance().put(Currency.BGN, cashBalanceCurrency);
//
//        when(fileUtilsHelper.readLastBalance()).thenReturn(cashBalanceDTO);
//        when(cashBalanceMapper.toCashBalance(cashBalanceDTO)).thenReturn(cashBalance);
//        when(cashBalanceMapper.toCashBalanceDTO(any(CashBalance.class))).thenReturn(cashBalanceDTO);
//
//        CashOperationDTO result = operationService.performCashOperation(cashOperationDTO);
//        assertEquals(cashOperationDTO, result);
//        assertEquals(BigDecimal.valueOf(100), cashBalanceCurrency.getTotalBalance());
//        assertEquals(10, (int) cashBalanceCurrency.getDenomination().get(10));
//
//        verify(fileUtilsHelper, times(1)).writeCashBalance(any(CashBalanceDTO.class));
//        verify(fileUtilsHelper, times(1)).writeCashOperation(any(CashOperationDTO.class));
//    }
//
//    // Failure Tests
//
//    @Test
//    void testDepositOperationThrowsInvalidDenominationException() throws IOException, IllegalAccessException, NoSuchFieldException {
//        denominations = new BanknotesDenominationsConfig();
//        Map<String, Set<Integer>> denominationMap = new HashMap<>();
//
//        Set<Integer> bgnDenominations = new HashSet<>();
//        Collections.addAll(bgnDenominations, 10, 20, 50, 100);
//        denominationMap.put("BGN", bgnDenominations);
//
//        denominations.setDenominations(denominationMap);
//
//        Field denominationsField = OperationServiceImpl.class.getDeclaredField("denominations");
//        denominationsField.setAccessible(true);
//        denominationsField.set(operationService, denominations);
//
//        CashOperationDTO cashOperationDTO = new CashOperationDTO();
//        cashOperationDTO.setType(CashOperationType.DEPOSIT);
//        cashOperationDTO.setCurrency(Currency.BGN);
//        Map<Integer, Integer> operationDenominations = new HashMap<>();
//        operationDenominations.put(25, 10);  // Invalid denomination
//        cashOperationDTO.setDenomination(operationDenominations);
//
//        assertThrows(InvalidBanknoteDenominationException.class, () -> operationService.performCashOperation(cashOperationDTO));
//    }
//
//    @Test
//    void testWithdrawalOperationThrowsInvalidDenominationException() throws IOException, IllegalAccessException, NoSuchFieldException {
//        denominations = new BanknotesDenominationsConfig();
//        Map<String, Set<Integer>> denominationMap = new HashMap<>();
//
//        Set<Integer> bgnDenominations = new HashSet<>();
//        Collections.addAll(bgnDenominations, 10, 20, 50, 100);
//        denominationMap.put("BGN", bgnDenominations);
//
//        denominations.setDenominations(denominationMap);
//
//        Field denominationsField = OperationServiceImpl.class.getDeclaredField("denominations");
//        denominationsField.setAccessible(true);
//        denominationsField.set(operationService, denominations);
//
//        CashOperationDTO cashOperationDTO = new CashOperationDTO();
//        cashOperationDTO.setType(CashOperationType.WITHDRAWAL);
//        cashOperationDTO.setCurrency(Currency.BGN);
//        cashOperationDTO.setAmount(BigDecimal.valueOf(100));  // Set the amount field
//        Map<Integer, Integer> operationDenominations = new HashMap<>();
//        operationDenominations.put(25, 10);  // Invalid denomination
//        cashOperationDTO.setDenomination(operationDenominations);
//
//        assertThrows(InvalidBanknoteDenominationException.class, () -> operationService.performCashOperation(cashOperationDTO));
//    }
//
//    @Test
//    void testWithdrawalOperationThrowsInvalidQuantityException() throws IOException {
//        CashOperationDTO cashOperationDTO = new CashOperationDTO();
//        cashOperationDTO.setType(CashOperationType.WITHDRAWAL);
//        cashOperationDTO.setCurrency(Currency.BGN);
//        Map<Integer, Integer> operationDenominations = new HashMap<>();
//        operationDenominations.put(10, -10);
//        cashOperationDTO.setDenomination(operationDenominations);
//
//        assertThrows(InvalidBanknoteQuantityException.class, () -> operationService.performCashOperation(cashOperationDTO));
//    }
//}
