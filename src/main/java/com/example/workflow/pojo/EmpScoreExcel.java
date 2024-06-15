package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmpScoreExcel {
    @ExcelProperty("姓名")
    private String empName;
    @ExcelProperty("评分条目")
    private String target;
    @ExcelProperty("条目占比")
    private BigDecimal scorePercent;
    @ExcelProperty("备注")
    private String ins;
    @ExcelProperty("评分人")
    private String assessorName;
    @ExcelProperty("评分人占比")
    private BigDecimal assessorPercent;
    @ExcelProperty("得分")
    private BigDecimal score;
    @ExcelProperty("评分人占比")
    private BigDecimal correctedValue;
}
