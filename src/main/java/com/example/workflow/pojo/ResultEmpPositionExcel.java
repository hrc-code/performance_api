package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResultEmpPositionExcel {
    @ExcelProperty("岗位")
    private String position;
    @ExcelProperty("员工")
    private String empName;
    @ExcelProperty("岗位占比")
    private BigDecimal posiPercent;
    @ExcelProperty("所属部门")
    private String deptName;
}
