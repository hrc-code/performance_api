package com.example.workflow.model.dto;

import com.example.workflow.model.entity.CoefficientView;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EmpCoeForm {
    private List<CoefficientView> empList;
    private BigDecimal baseWage;
    private BigDecimal performanceWage;
    private Integer option;
}
