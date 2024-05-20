package com.example.workflow.feedback;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PieceError {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("计件条目")
    private String name;
    @ExcelProperty("条目一")
    private String target1;
    @ExcelProperty("条目二")
    private String target2;
    @ExcelProperty("单价")
    private BigDecimal targetNum;
    @ExcelProperty("备注")
    private String ins;
    @ExcelProperty("错误原因")
    private String error;
}
