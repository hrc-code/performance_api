package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PieceExcel {
    @ExcelProperty("评分条目")
    private String name;
    @ExcelProperty("条目一")
    private String target1;
    @ExcelProperty("条目二")
    private String target2;
    @ExcelProperty("单价")
    private BigDecimal targetNum;
    @ExcelProperty("备注")
    private String ins;
}
