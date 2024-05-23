package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ResultEmpPieceExcel {
    @ExcelProperty("姓名")
    private String empName;
    @ExcelProperty("计件条目")
    private String name;
    @ExcelProperty("数量")
    private BigDecimal targetNum;
    @ExcelProperty("单价")
    private Integer workOrder;
    @ExcelProperty("质量")
    private BigDecimal quality;
}
