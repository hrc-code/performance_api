package com.example.workflow.model.pojo;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PositionKpiExcel {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("岗位名称")
    private String position;
    @ExcelProperty("所属部门")
    private String dept;
    @ExcelProperty("提成条目")
    private String piece;
}
