package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeRewardExcel {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("工号")
    private String num;
    @ExcelProperty("员工姓名")
    private String employeeName;
    @ExcelProperty("特定绩效考核依据(文件名）")
    private String fileName;
    @ExcelProperty("金额")
    private BigDecimal reward;
    @ExcelProperty("员工岗位")
    private String positionName;
    @ExcelProperty("所属部门")
    private String dept;
}
