package com.example.workflow.model.feedback;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PositionError {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("部门名称")
    private String dept;
    @ExcelProperty("岗位名称")
    private String position;
    @ExcelProperty("岗位类型")
    private String type;
    @ExcelProperty("绩效挂钩")
    private String kind;
    @ExcelProperty("错误原因")
    private String error;
}
