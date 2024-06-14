package com.fibank.cashdesk.service.impl;

import com.fibank.cashdesk.config.BanknotesDenominationsConfig;
import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.dto.CashOperationDTO;
import com.fibank.cashdesk.entity.CashBalanceCurrency;
import com.fibank.cashdesk.entity.CashBalance;
import com.fibank.cashdesk.enums.CashOperationType;
import com.fibank.cashdesk.enums.Currency;
import com.fibank.cashdesk.exception.InvalidBanknoteDenominationException;
import com.fibank.cashdesk.exception.InvalidBanknoteQuantityException;
import com.fibank.cashdesk.mapper.CashBalanceMapper;
import com.fibank.cashdesk.mapper.CashOperationMapper;
import com.fibank.cashdesk.service.OperationService;
import com.fibank.cashdesk.utils.FileUtilsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class OperationServiceImpl implements OperationService {

    @Value("${api-key}")
    private String apiKey;

    private CashBalance cashBalance;

    private CashBalanceMapper cashBalanceMapper;
    private CashOperationMapper cashOperationMapper;
    private BanknotesDenominationsConfig denominations;

    public OperationServiceImpl(CashBalanceMapper cashBalanceMapper, CashOperationMapper cashOperationMapper, BanknotesDenominationsConfig denominations) {
        this.cashBalanceMapper = cashBalanceMapper;
        this.cashOperationMapper = cashOperationMapper;
        this.denominations = denominations;
    }

    @Override
    public boolean isApiKeyValid(String apiKey) {
        return this.apiKey.equals(apiKey);
    }

    @Override
    public CashBalanceDTO retrieveCashBalance() throws IOException {
        CashBalanceDTO cashBalanceDTO = FileUtilsHelper.readLastBalance();
        cashBalance = cashBalanceMapper.toCashBalance(cashBalanceDTO);

        return cashBalanceDTO;
    }

    @Override
    public CashOperationDTO performCashOperation(CashOperationDTO cashOperationDTO) throws IOException {
        CashOperationType operation = cashOperationDTO.getType();

        retrieveCashBalance();
        performValidations(cashOperationDTO);

        if (operation.equals(CashOperationType.WITHDRAWAL)) {
            withdrawCash(cashOperationDTO);
        } else if (operation.equals(CashOperationType.DEPOSIT)) {
            depositCash(cashOperationDTO);
        }

        FileUtilsHelper.writeCashBalance(cashBalanceMapper.toCashBalanceDTO(cashBalance));
        FileUtilsHelper.writeCashOperation(cashOperationDTO);

        return cashOperationDTO;
    }

    @PostConstruct
    private void init() throws IOException {
        if (FileUtilsHelper.isCashBalanceFileEmpty()) {
            CashBalance initialBalance = new CashBalance().setBalance(createInitialBalances());
            FileUtilsHelper.writeCashBalance(cashBalanceMapper.toCashBalanceDTO(initialBalance));
        }
    }

    private void depositCash(CashOperationDTO cashOperationDTO) {
        CashBalanceCurrency cashBalanceCurrency = cashBalance.getBalance().get(cashOperationDTO.getCurrency());

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

    private void withdrawCash(CashOperationDTO cashOperationDTO) {
        CashBalanceCurrency cashBalanceCurrency = cashBalance.getBalance().get(cashOperationDTO.getCurrency());
        BigDecimal totalBalance = cashBalanceCurrency.getTotalBalance();

        for (Map.Entry<Integer, Integer> entry : cashOperationDTO.getDenomination().entrySet()) {
            int banknote = entry.getKey();
            int count = entry.getValue();

            cashBalanceCurrency.getDenomination().put(banknote, cashBalanceCurrency.getDenomination().get(banknote) - count);
            totalBalance = totalBalance.subtract(BigDecimal.valueOf(banknote).multiply(BigDecimal.valueOf(count)));
        }

        cashBalanceCurrency.setTotalBalance(totalBalance);
    }

    private void performValidations(CashOperationDTO cashOperationDTO) {
        validateBanknotesDenomination(cashOperationDTO);
        validateNegativeBanknotesCount(cashOperationDTO);
        validateSufficientBalance(cashOperationDTO);
    }

    private void validateSufficientBalance(CashOperationDTO cashOperationDTO) {
        if (cashOperationDTO.getType().equals(CashOperationType.WITHDRAWAL)) {
            Currency currency = cashOperationDTO.getCurrency();
            Map<Integer, Integer> currentOperationDenominations = cashOperationDTO.getDenomination();

            CashBalanceCurrency cashBalanceCurrency = cashBalance.getBalance().get(currency);

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
