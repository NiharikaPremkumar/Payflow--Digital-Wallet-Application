package com.payflow.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AddFundsRequest {
    private BigDecimal amount;
}
