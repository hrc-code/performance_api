package com.example.workflow.model.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResultEmpPieceExcel {
    @ExcelProperty("姓名")
    private String empName;
    @ExcelProperty("计件条目")
    private String name;
    @ExcelProperty("单价")
    private BigDecimal targetNum;
    @ExcelProperty("数量")
    private Integer workOrder;
    @ExcelProperty("质量")
    private BigDecimal quality;
}
