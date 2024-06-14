package com.fibank.cashdesk.mapper;

import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.dto.CashOperationDTO;
import com.fibank.cashdesk.entity.CashBalance;
import com.fibank.cashdesk.entity.CashOperation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CashOperationMapper {

    CashOperation toCashOperation(CashOperationDTO cashOperationDTO);

    CashOperationDTO toCashOperationDTO(CashOperation cashOperation);
}
