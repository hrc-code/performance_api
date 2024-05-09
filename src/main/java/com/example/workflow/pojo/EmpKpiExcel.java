package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmpKpiExcel {
    @ExcelProperty("序号")
    private Integer order;
    @ExcelProperty("工号")
    private String num;
    @ExcelProperty("员工姓名")
    private String employeeName;
    @ExcelProperty("员工岗位")
    private String positionName;
    @ExcelProperty("所属部门")
    private String dept;
    @ExcelProperty("提成模版（1或2）")
    private Integer type;
    @ExcelProperty("提成条目")
    private String kpiName;
    @ExcelProperty("条目一数值")
    private BigDecimal inTarget1;
    @ExcelProperty("条目二数值")
    private BigDecimal inTarget2;
}
