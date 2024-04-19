package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegionExcel {
    @ExcelProperty("地区")
    private String region;
    @ExcelProperty("地区系数")
    private Double coefficient;
}
