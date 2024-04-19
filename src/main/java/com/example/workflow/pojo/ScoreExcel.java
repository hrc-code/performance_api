package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ScoreExcel {
    @ExcelProperty("评分条目")
    private String target;
    @ExcelProperty("备注")
    private String ins;
}
