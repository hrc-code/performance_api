package com.example.workflow.feedback;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ScoreError {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("评分条目")
    private String target;
    @ExcelProperty("备注")
    private String ins;
    @ExcelProperty("错误原因")
    private String error;
}
