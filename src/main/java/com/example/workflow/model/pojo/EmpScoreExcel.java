package com.example.workflow.model.pojo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmpScoreExcel {

    @ExcelProperty("姓名")
    private String empName;
    @ExcelProperty("岗位名称")
    private String position;
    @ExcelProperty("部门")
    private String deptName;
    @ExcelProperty("考核条目")
    private String target;
    @ExcelProperty("评分")
    private BigDecimal score;
    @ExcelProperty("评分占比%")
    private String scorePercent;
    @ExcelProperty("评分人")
    private String assessorName;
    @ExcelProperty("评分人占比%")
    private String assessorPercent;
    @ExcelProperty("得分")
    private BigDecimal grade;
    @ExcelProperty("修正值")
    private int correctedValue;


//    @ExcelProperty("当前状态")
//    private String state;
//    @ExcelProperty("考核条目备注")
//    private String ins;
}
