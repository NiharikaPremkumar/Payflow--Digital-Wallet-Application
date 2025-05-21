package com.payflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionAuditDTO {
    private Long id;
    private String senderUsername;
    private String receiverUsername;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private String status;
}
