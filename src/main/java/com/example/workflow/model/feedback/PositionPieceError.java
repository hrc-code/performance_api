package com.example.workflow.model.feedback;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PositionPieceError {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("岗位名称")
    private String position;
    @ExcelProperty("所属部门")
    private String dept;
    @ExcelProperty("计件条目")
    private String piece;
    @ExcelProperty("错误原因")
    private String error;
}
