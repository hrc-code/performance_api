package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KpiExcel {
    @ExcelProperty("kpi条目")
    private String name;
    @ExcelProperty("条目一名称")
    private String target1;
    @ExcelProperty("条目二名称")
    private String target2;
    @ExcelProperty("规则序号（升序,从1开始）")
    private Integer kpiKey;
    @ExcelProperty("条目二百分比")
    private BigDecimal rulePercent;
    @ExcelProperty("结算百分比")
    private BigDecimal resultPercent;
}
