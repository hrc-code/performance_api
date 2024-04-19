package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeExcel {
    @ExcelProperty("工号")
    private String num;
    @ExcelProperty("姓名")
    private String name;
    @ExcelProperty("联系电话1")
    private String phoneNum1;
    @ExcelProperty("联系电话2")
    private String phoneNum2;
    @ExcelProperty("邮箱")
    private String email;
    @ExcelProperty("身份证号")
    private String idNum;
    @ExcelProperty("出生年月")
    private String birthday;
    @ExcelProperty("通信地址")
    private String address;
    @ExcelProperty("职务")
    private String roleName;
    @ExcelProperty("岗位")
    private String position;
    @ExcelProperty("地域")
    private String region;
    @ExcelProperty("岗位绩效")
    private BigDecimal positionCoefficient;
    @ExcelProperty("部门名")
    private String deptName;
    @ExcelProperty("岗位类型")
    private String typeName;
}
