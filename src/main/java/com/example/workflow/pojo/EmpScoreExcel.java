package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmpScoreExcel {
    @ExcelProperty("评分条目")
    private String target;
    @ExcelProperty("备注")
    private String ins;
    @ExcelProperty("评分人")
    private String assessorName;
    @ExcelProperty("评分人占比")
    private BigDecimal assessorPercent;
    private BigDecimal scorePercent;
    private String empName;
    private BigDecimal score;
    private BigDecimal correctedValue;
}
