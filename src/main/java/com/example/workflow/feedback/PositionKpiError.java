package com.example.workflow.feedback;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PositionKpiError {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("岗位名称")
    private String position;
    @ExcelProperty("所属部门")
    private String dept;
    @ExcelProperty("提成条目")
    private String piece;
    @ExcelProperty("错误原因")
    private String error;
}
