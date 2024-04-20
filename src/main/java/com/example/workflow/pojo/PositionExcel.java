package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionExcel {
    @ExcelProperty("部门名称")
    private String dept;
    @ExcelProperty("岗位名称")
    private String position;
    @ExcelProperty("岗位类型")
    private String type;
    @ExcelProperty("绩效挂钩")
    private String kind;
}
