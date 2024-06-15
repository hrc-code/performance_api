package com.example.workflow.model.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class OkrExcel {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("目标")
    private String target;
    @ExcelProperty("责任人")
    private String empName;
    @ExcelProperty("责任人岗位")
    private String position;
    @ExcelProperty("责任人所属部门")
    private String dept;
    @ExcelProperty("O分值")
    private Double Oscore;
    @ExcelProperty("子目标")
    private String keyResult;
    @ExcelProperty("KR权重")
    private Double keyWeight;
    @ExcelProperty("评分人")
    private String assessorName;
    @ExcelProperty("评分人工号")
    private String assessorNum;
    @ExcelProperty("考核年月（年.月）")
    private String yearMonth;
}
