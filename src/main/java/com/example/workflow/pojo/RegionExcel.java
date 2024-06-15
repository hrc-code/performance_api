package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class RegionExcel {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("地区")
    private String region;
    @ExcelProperty("地区系数")
    private Double coefficient;
}
