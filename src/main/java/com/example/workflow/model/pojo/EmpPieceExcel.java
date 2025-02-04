package com.example.workflow.model.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmpPieceExcel {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("工号")
    private String num;
    @ExcelProperty("员工姓名")
    private String employeeName;
    @ExcelProperty("员工岗位")
    private String positionName;
    @ExcelProperty("所属部门")
    private String dept;
    @ExcelProperty("计件条目")
    private String pieceName;
    @ExcelProperty("数量")
    private Integer quantity;
    @ExcelProperty("质量（100%）")
    private BigDecimal quality;
}
