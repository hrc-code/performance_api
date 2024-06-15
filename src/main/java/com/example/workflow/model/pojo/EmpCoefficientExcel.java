package com.example.workflow.model.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmpCoefficientExcel {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("员工工号")
    private String num;
    @ExcelProperty("员工姓名")
    private String empName;
    @ExcelProperty("岗位系数")
    private BigDecimal positionCoefficient;
    @ExcelProperty("服务地区")
    private String region;
    @ExcelProperty("基础工资")
    private BigDecimal wage;
    @ExcelProperty("绩效工资")
    private BigDecimal performanceWage;
}
