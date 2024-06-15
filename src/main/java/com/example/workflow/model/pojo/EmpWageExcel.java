package com.example.workflow.model.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmpWageExcel {
    @ExcelProperty("姓名")
    private String name;
    @ExcelProperty("岗位")
    private String position;
    @ExcelProperty("评分绩效结算")
    private BigDecimal scoreWage;
    @ExcelProperty("计件绩效结算")
    private BigDecimal pieceWage;
    @ExcelProperty("提成绩效结算")
    private BigDecimal kpiWage;
    @ExcelProperty("OKR绩效结算")
    private BigDecimal okrWage;
    @ExcelProperty("特殊绩效结算")
    private BigDecimal rewardWage;
    @ExcelProperty("总计")
    private BigDecimal total;
}
