package com.example.workflow.feedback;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionScoreError {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("岗位名称")
    private String position;
    @ExcelProperty("所属部门")
    private String dept;
    @ExcelProperty("评分条目")
    private String score;
    @ExcelProperty("条目占比")
    private BigDecimal scorePercent;
    @ExcelProperty("员工工号")
    private String num;
    @ExcelProperty("评分人")
    private String assessorName;
    @ExcelProperty("评分人占比")
    private Float assessorPercent;
    @ExcelProperty("错误原因")
    private String error;
}
