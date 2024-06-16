package com.fibank.cashdesk.service;

import com.fibank.cashdesk.config.BanknotesDenominationsConfig;
import com.fibank.cashdesk.dto.CashBalanceCurrencyDTO;
import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.dto.CashOperationDTO;
import com.fibank.cashdesk.entity.CashBalanceCurrency;
import com.fibank.cashdesk.entity.CashBalance;
import com.fibank.cashdesk.enums.CashOperationType;
import com.fibank.cashdesk.enums.Currency;
import com.fibank.cashdesk.exception.InvalidBanknoteDenominationException;
import com.fibank.cashdesk.exception.InvalidBanknoteQuantityException;
import com.fibank.cashdesk.mapper.CashBalanceMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


@Service
public class OperationService {

    private final String apiKey;
    private final CashBalanceMapper cashBalanceMapper;
    private final BanknotesDenominationsConfig denominations;
    private final FileUtilsService fileUtilsService;


    public OperationService(CashBalanceMapper cashBalanceMapper,
                            BanknotesDenominationsConfig denominations,
                            FileUtilsService fileUtilsService,
                            @Value("${api-key}") String apiKey) {
        this.cashBalanceMapper = cashBalanceMapper;
        this.denominations = denominations;
        this.fileUtilsService = fileUtilsService;
        this.apiKey = apiKey;
    }

    public boolean isApiKeyValid(String apiKey) {
        return this.apiKey.equals(apiKey);
    }

    public CashBalanceDTO retrieveCashBalance() throws IOException {
        return fileUtilsService.readLastBalance();
    }

    public CashOperationDTO performCashOperation(CashOperationDTO cashOperationDTO) throws IOException {
        CashOperationType operation = cashOperationDTO.getType();
        CashBalanceDTO balanceDTO = retrieveCashBalance();
        performValidations(cashOperationDTO, balanceDTO);

        if (operation == CashOperationType.WITHDRAWAL) {
            withdrawCash(cashOperationDTO, balanceDTO);
        } else if (operation == CashOperationType.DEPOSIT) {
            depositCash(cashOperationDTO, balanceDTO);
        }

        fileUtilsService.writeCashBalance(balanceDTO);
        fileUtilsService.writeCashOperation(cashOperationDTO);

        return cashOperationDTO;
    }

    @PostConstruct
    private void init() throws IOException {
        if (fileUtilsService.isCashBalanceFileEmpty()) {
            CashBalance initialBalance = new CashBalance().setBalance(createInitialBalances());
            fileUtilsService.writeCashBalance(cashBalanceMapper.toCashBalanceDTO(initialBalance));
        }
    }


    private void depositCash(CashOperationDTO cashOperationDTO, CashBalanceDTO balanceDTO) {
        CashBalanceCurrencyDTO cashBalanceCurrency = balanceDTO.getBalance().get(cashOperationDTO.getCurrency());

        BigDecimal totalBalance = cashBalanceCurrency.getTotalBalance();
        for (Map.Entry<Integer, Integer> entry : cashOperationDTO.getDenomination().entrySet()) {
            int banknote = entry.getKey();
            int count = entry.getValue();

            cashBalanceCurrency.getDenomination().putIfAbsent(banknote, 0);
            cashBalanceCurrency.getDenomination().put(banknote, cashBalanceCurrency.getDenomination().get(banknote) + count);

            totalBalance = totalBalance.add(BigDecimal.valueOf(banknote).multiply(BigDecimal.valueOf(count)));
        }
        cashBalanceCurrency.setTotalBalance(totalBalance);
    }

    private void withdrawCash(CashOperationDTO cashOperationDTO, CashBalanceDTO balanceDTO) {
        CashBalanceCurrencyDTO cashBalanceCurrency = balanceDTO.getBalance().get(cashOperationDTO.getCurrency());
        BigDecimal totalBalance = cashBalanceCurrency.getTotalBalance();

        for (Map.Entry<Integer, Integer> entry : cashOperationDTO.getDenomination().entrySet()) {
            int banknote = entry.getKey();
            int count = entry.getValue();

            cashBalanceCurrency.getDenomination().put(banknote, cashBalanceCurrency.getDenomination().get(banknote) - count);
            totalBalance = totalBalance.subtract(BigDecimal.valueOf(banknote).multiply(BigDecimal.valueOf(count)));
        }

        cashBalanceCurrency.setTotalBalance(totalBalance);
    }

    private void performValidations(CashOperationDTO cashOperationDTO, CashBalanceDTO balanceDTO) {
        validateBanknotesDenomination(cashOperationDTO);
        validateNegativeBanknotesCount(cashOperationDTO);
        validateSufficientBalance(cashOperationDTO, balanceDTO);
    }

    private void validateSufficientBalance(CashOperationDTO cashOperationDTO, CashBalanceDTO balanceDTO) {
        if (cashOperationDTO.getType().equals(CashOperationType.WITHDRAWAL)) {
            Currency currency = cashOperationDTO.getCurrency();
            Map<Integer, Integer> currentOperationDenominations = cashOperationDTO.getDenomination();

            CashBalanceCurrencyDTO cashBalanceCurrency = balanceDTO.getBalance().get(currency);

            BigDecimal requestedWithdrawalAmount = cashOperationDTO.getAmount();
            BigDecimal banknotesAmount = BigDecimal.ZERO;
            for (Map.Entry<Integer, Integer> entry : currentOperationDenominations.entrySet()) {
                int banknote = entry.getKey();
                int count = entry.getValue();

                banknotesAmount = banknotesAmount.add(BigDecimal.valueOf(banknote).multiply(BigDecimal.valueOf(count)));

                Integer availableCount = cashBalanceCurrency.getDenomination().get(banknote);
                if (availableCount == null || availableCount < count) {
                    String message = "Insufficient quantity of banknote denomination: " + banknote +
                            " for currency: " + currency.name() + ". Requested: " + count + ", Available: " + (availableCount == null ? 0 : availableCount);
                    throw new InvalidBanknoteQuantityException(message);
                }
            }

            if (!requestedWithdrawalAmount.equals(banknotesAmount)) {
                String message = "Invalid withdrawal amount: Requested amount " + requestedWithdrawalAmount +
                        " does not match the total banknotes amount " + banknotesAmount;
                throw new IllegalArgumentException(message);
            }
        }
    }

    private void validateNegativeBanknotesCount(CashOperationDTO cashOperationDTO) {
        Map<Integer, Integer> currentOperationDenominations = cashOperationDTO.getDenomination();

        for (Integer count : currentOperationDenominations.values()) {
            if (count < 0) {
                throw new InvalidBanknoteQuantityException("Banknotes count must be greater than zero");
            }
        }
    }

    private void validateBanknotesDenomination(CashOperationDTO cashOperationDTO) {

        Set<Integer> banknotesDenominations = denominations.getDenominations().get(cashOperationDTO.getCurrency().name());
        Map<Integer, Integer> currentOperationDenominations = cashOperationDTO.getDenomination();

        for (Integer banknote : currentOperationDenominations.keySet()) {
            if (!banknotesDenominations.contains(banknote)) {
                throw new InvalidBanknoteDenominationException("Invalid banknote denomination: " + banknote + " does not exist for currency: " + cashOperationDTO.getCurrency().name());
            }
        }
    }

    private Map<Currency, CashBalanceCurrency> createInitialBalances() {
        Map<Currency, CashBalanceCurrency> balances = new HashMap<>();

        balances.put(Currency.BGN, createBGNBalance());
        balances.put(Currency.EUR, createEURBalance());

        return balances;
    }

    private CashBalanceCurrency createBGNBalance() {
        CashBalanceCurrency bgnBalance = new CashBalanceCurrency();
        bgnBalance.setTotalBalance(BigDecimal.valueOf(1000));

        Map<Integer, Integer> bgnDenomination = new HashMap<>();
        bgnDenomination.put(10, 50);
        bgnDenomination.put(50, 10);

        bgnBalance.setDenomination(bgnDenomination);

        return bgnBalance;
    }

    private CashBalanceCurrency createEURBalance() {
        CashBalanceCurrency eurBalance = new CashBalanceCurrency();
        eurBalance.setTotalBalance(BigDecimal.valueOf(2000));

        Map<Integer, Integer> eurDenomination = new HashMap<>();
        eurDenomination.put(10, 100);
        eurDenomination.put(50, 20);

        eurBalance.setDenomination(eurDenomination);

        return eurBalance;
    }


}

