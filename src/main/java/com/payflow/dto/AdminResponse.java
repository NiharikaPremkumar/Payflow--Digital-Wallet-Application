package com.payflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminResponse {
    private boolean success;
    private String message;
    private Object data;
}