package com.fibank.cashdesk.mapper;

import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.entity.CashBalance;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CashBalanceMapper {

    CashBalance toCashBalance(CashBalanceDTO cashBalanceDTO);

    CashBalanceDTO toCashBalanceDTO(CashBalance cashBalance);
}
