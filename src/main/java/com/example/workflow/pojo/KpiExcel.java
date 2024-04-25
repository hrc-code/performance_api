package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;

import java.math.BigDecimal;

public class KpiExcel {
    @ExcelProperty("kpi条目")
    private String name;
    @ExcelProperty("条目一名称")
    private String target1;
    @ExcelProperty("条目二名称")
    private String target2;
    @ExcelProperty("规则序号（升序）")
    private BigDecimal kpiKey;
    @ExcelProperty("规则一")
    private BigDecimal rulePercent;
    @ExcelProperty("规则二")
    private String ins;
}
