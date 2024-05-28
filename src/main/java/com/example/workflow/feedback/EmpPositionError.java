package com.example.workflow.feedback;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmpPositionError {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("员工")
    private String empName;
    @ExcelProperty("员工工号")
    private String empNum;
    @ExcelProperty("所属部门（请以，划分）")
    protected String dept;
    @ExcelProperty("岗位（请以，划分）")
    private String position;
    @ExcelProperty("岗位占比（请以，划分）")
    private BigDecimal posiPercent;
    @ExcelProperty("错误原因")
    private String error;
}
