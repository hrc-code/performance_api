package com.example.workflow.model.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Order3 {
    private BigDecimal inTarget1;
    private BigDecimal inTarget2;
    private Double outNum;
}
