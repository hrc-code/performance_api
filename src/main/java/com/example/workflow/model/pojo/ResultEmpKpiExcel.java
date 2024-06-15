package com.example.workflow.model.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResultEmpKpiExcel {
    @ExcelProperty("姓名")
    private String empName;
    @ExcelProperty("提成条目")
    private String name;
    @ExcelProperty("提成条目一")
    private String target1;
    @ExcelProperty("提成条目二")
    private String target2;
    @ExcelProperty("条目一数值")
    private BigDecimal inTarget1;
    @ExcelProperty("条目二数值")
    private BigDecimal inTarget2;
    @ExcelProperty("结算")
    private BigDecimal result;
    @ExcelProperty("修正值")
    private BigDecimal correctedValue;
}

