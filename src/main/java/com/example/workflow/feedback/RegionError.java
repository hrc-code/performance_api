package com.example.workflow.feedback;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class RegionError {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("地区")
    private String region;
    @ExcelProperty("地区系数")
    private Double coefficient;
    @ExcelProperty("错误原因")
    private String error;
}
