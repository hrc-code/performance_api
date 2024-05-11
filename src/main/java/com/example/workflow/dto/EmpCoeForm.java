package com.example.workflow.dto;

import com.example.workflow.entity.CoefficientView;
import com.example.workflow.entity.EmpCoefficient;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EmpCoeForm {
    private List<CoefficientView> empList;
    private BigDecimal baseWage;
}
