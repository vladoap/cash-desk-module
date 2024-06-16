package com.fibank.cashdesk.service;

import com.fibank.cashdesk.config.BanknotesDenominationsConfig;
import com.fibank.cashdesk.dto.CashBalanceCurrencyDTO;
import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.dto.CashOperationDTO;
import com.fibank.cashdesk.entity.CashBalanceCurrency;
import com.fibank.cashdesk.enums.CashOperationType;
import com.fibank.cashdesk.enums.Currency;
import com.fibank.cashdesk.exception.InvalidBanknoteDenominationException;
import com.fibank.cashdesk.exception.InvalidBanknoteQuantityException;
import com.fibank.cashdesk.mapper.CashBalanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OperationServiceTest {


    private OperationService operationService;

    @Mock
    private CashBalanceMapper cashBalanceMapper;

    @Mock
    private BanknotesDenominationsConfig denominations;

    @Mock
    private FileUtilsService fileUtilsService;

    @BeforeEach
    public void setUp() {
        String apiKey = "mocked-api-key";
        operationService = new OperationService(cashBalanceMapper, denominations, fileUtilsService, apiKey);
        when(denominations.getDenominations()).thenReturn(getMockedDenominations());
    }

    private Map<String, Set<Integer>> getMockedDenominations() {
        Map<String, Set<Integer>> denominations = new HashMap<>();
        denominations.put(Currency.BGN.name(), new HashSet<>(Arrays.asList(5, 10, 20, 50, 100)));
        denominations.put(Currency.EUR.name(), new HashSet<>(Arrays.asList(5, 10, 20, 50, 100, 200, 500)));
        return denominations;
    }

    @Test
    public void testPerformCashOperationShouldDeposit() throws IOException {
        CashBalanceDTO balanceDTO = new CashBalanceDTO();
        Map<Currency, CashBalanceCurrencyDTO> balances = new HashMap<>();

        CashBalanceCurrencyDTO bgnBalance = new CashBalanceCurrencyDTO();
        bgnBalance.setTotalBalance(BigDecimal.valueOf(1000));
        bgnBalance.setDenomination(new HashMap<>());
        balances.put(Currency.BGN, bgnBalance);
        balanceDTO.setBalance(balances);

        CashOperationDTO operationDTO = new CashOperationDTO();
        operationDTO.setType(CashOperationType.DEPOSIT);
        operationDTO.setCurrency(Currency.BGN);
        operationDTO.setDenomination(new HashMap<>());
        operationDTO.getDenomination().put(10, 10);

        when(fileUtilsService.readLastBalance()).thenReturn(balanceDTO);

        operationService.performCashOperation(operationDTO);

        assertEquals(BigDecimal.valueOf(1100), bgnBalance.getTotalBalance());
        assertEquals(10, (int) bgnBalance.getDenomination().get(10));
    }

    @Test
    public void testPerformCashOperationShouldWithdraw() throws IOException {
        CashBalanceDTO balanceDTO = new CashBalanceDTO();
        Map<Currency, CashBalanceCurrencyDTO> balances = new HashMap<>();
        CashBalanceCurrencyDTO bgnBalance = new CashBalanceCurrencyDTO();
        bgnBalance.setTotalBalance(BigDecimal.valueOf(1000));
        Map<Integer, Integer> bgnDenominations = new HashMap<>();
        bgnDenominations.put(10, 20);
        bgnBalance.setDenomination(bgnDenominations);
        balances.put(Currency.BGN, bgnBalance);
        balanceDTO.setBalance(balances);

        CashOperationDTO operationDTO = new CashOperationDTO();
        operationDTO.setType(CashOperationType.WITHDRAWAL);
        operationDTO.setCurrency(Currency.BGN);
        Map<Integer, Integer> withdrawDenominations = new HashMap<>();
        withdrawDenominations.put(10, 5);
        operationDTO.setDenomination(withdrawDenominations);
        operationDTO.setAmount(BigDecimal.valueOf(50));

        when(fileUtilsService.readLastBalance()).thenReturn(balanceDTO);

        operationService.performCashOperation(operationDTO);

        assertEquals(BigDecimal.valueOf(950), bgnBalance.getTotalBalance());
        assertEquals(15, (int) bgnBalance.getDenomination().get(10));
    }

    @Test
    public void testPerformCashOperationDepositShouldThrowWhenInvalidDenomination() throws IOException {
        CashBalanceDTO balanceDTO = new CashBalanceDTO();
        Map<Currency, CashBalanceCurrencyDTO> balances = new HashMap<>();
        CashBalanceCurrencyDTO bgnBalance = new CashBalanceCurrencyDTO();
        bgnBalance.setTotalBalance(BigDecimal.valueOf(1000));
        bgnBalance.setDenomination(new HashMap<>());
        balances.put(Currency.BGN, bgnBalance);
        balanceDTO.setBalance(balances);

        CashOperationDTO operationDTO = new CashOperationDTO();
        operationDTO.setType(CashOperationType.DEPOSIT);
        operationDTO.setCurrency(Currency.BGN);
        operationDTO.setDenomination(new HashMap<>());
        operationDTO.getDenomination().put(3, 1);

        when(fileUtilsService.readLastBalance()).thenReturn(balanceDTO);

        assertThrows(InvalidBanknoteDenominationException.class,
                () -> operationService.performCashOperation(operationDTO));
    }

    @Test
    public void testPerformCashOperationDepositShouldThrowWhenInvalidBanknoteQuantity() throws IOException {
        CashBalanceDTO balanceDTO = new CashBalanceDTO();
        CashBalanceCurrencyDTO bgnBalance = new CashBalanceCurrencyDTO();
        bgnBalance.setTotalBalance(BigDecimal.valueOf(1000));
        Map<Integer, Integer> bgnDenominations = new HashMap<>();
        bgnDenominations.put(10, 5);
        bgnBalance.setDenomination(bgnDenominations);

        Map<Currency, CashBalanceCurrencyDTO> balances = new HashMap<>();
        balances.put(Currency.BGN, bgnBalance);
        balanceDTO.setBalance(balances);

        CashOperationDTO operationDTO = new CashOperationDTO();
        operationDTO.setType(CashOperationType.WITHDRAWAL);
        operationDTO.setCurrency(Currency.BGN);
        operationDTO.setDenomination(new HashMap<>());
        operationDTO.getDenomination().put(10, 10);
        operationDTO.setAmount(BigDecimal.valueOf(100));

        when(fileUtilsService.readLastBalance()).thenReturn(balanceDTO);

        assertThrows(InvalidBanknoteQuantityException.class,
                () -> operationService.performCashOperation(operationDTO));
    }

    @Test
    public void testPerformCashOperationDepositShouldThrowWhenInvalidWithdrawalAmount() throws IOException {
        CashBalanceDTO balanceDTO = new CashBalanceDTO();
        CashBalanceCurrencyDTO bgnBalance = new CashBalanceCurrencyDTO();
        bgnBalance.setTotalBalance(BigDecimal.valueOf(1000));
        Map<Integer, Integer> bgnDenominations = new HashMap<>();
        bgnDenominations.put(10, 20);
        bgnBalance.setDenomination(bgnDenominations);

        Map<Currency, CashBalanceCurrencyDTO> balances = new HashMap<>();
        balances.put(Currency.BGN, bgnBalance);
        balanceDTO.setBalance(balances);

        CashOperationDTO operationDTO = new CashOperationDTO();
        operationDTO.setType(CashOperationType.WITHDRAWAL);
        operationDTO.setCurrency(Currency.BGN);
        operationDTO.setDenomination(new HashMap<>());
        operationDTO.getDenomination().put(10, 10);
        operationDTO.setAmount(BigDecimal.valueOf(50));

        when(fileUtilsService.readLastBalance()).thenReturn(balanceDTO);

        assertThrows(IllegalArgumentException.class,
                () -> operationService.performCashOperation(operationDTO));
    }

    @Test
    public void testPerformCashOperationDepositShouldThrowWhenNegativeBanknoteQuantity() {
        CashOperationDTO operationDTO = new CashOperationDTO();
        operationDTO.setCurrency(Currency.BGN);
        Map<Integer, Integer> denominations = new HashMap<>();
        denominations.put(10, -5);
        operationDTO.setDenomination(denominations);

        assertThrows(InvalidBanknoteQuantityException.class,
                () -> operationService.performCashOperation(operationDTO));
    }

    private Map<Currency, CashBalanceCurrencyDTO> createInitialBalancesDTO() {
        Map<Currency, CashBalanceCurrencyDTO> balances = new HashMap<>();
        CashBalanceCurrencyDTO bgnBalance = new CashBalanceCurrencyDTO();
        bgnBalance.setTotalBalance(BigDecimal.valueOf(1000));
        Map<Integer, Integer> bgnDenomination = new HashMap<>();
        bgnDenomination.put(10, 50);
        bgnDenomination.put(50, 10);
        bgnBalance.setDenomination(bgnDenomination);
        balances.put(Currency.BGN, bgnBalance);

        CashBalanceCurrencyDTO eurBalance = new CashBalanceCurrencyDTO();
        eurBalance.setTotalBalance(BigDecimal.valueOf(2000));
        Map<Integer, Integer> eurDenomination = new HashMap<>();
        eurDenomination.put(10, 100);
        eurDenomination.put(50, 20);
        eurBalance.setDenomination(eurDenomination);
        balances.put(Currency.EUR, eurBalance);

        return balances;
    }

    private Map<Currency, CashBalanceCurrency> createInitialBalances() {
        Map<Currency, CashBalanceCurrency> balances = new HashMap<>();

        CashBalanceCurrency bgnBalance = new CashBalanceCurrency();
        bgnBalance.setTotalBalance(BigDecimal.valueOf(1000));
        Map<Integer, Integer> bgnDenomination = new HashMap<>();
        bgnDenomination.put(10, 50);
        bgnDenomination.put(50, 10);
        bgnBalance.setDenomination(bgnDenomination);
        balances.put(Currency.BGN, bgnBalance);

        CashBalanceCurrency eurBalance = new CashBalanceCurrency();
        eurBalance.setTotalBalance(BigDecimal.valueOf(2000));
        Map<Integer, Integer> eurDenomination = new HashMap<>();
        eurDenomination.put(10, 100);
        eurDenomination.put(50, 20);
        eurBalance.setDenomination(eurDenomination);
        balances.put(Currency.EUR, eurBalance);

        return balances;
    }
}
