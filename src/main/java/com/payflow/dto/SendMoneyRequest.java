package com.payflow.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendMoneyRequest {
    private String receiverUsername;
    private BigDecimal amount;
}
