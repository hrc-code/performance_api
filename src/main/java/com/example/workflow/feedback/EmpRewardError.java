package com.example.workflow.feedback;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;

import java.math.BigDecimal;

@Data
public class EmpRewardError {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("工号")
    private String num;
    @ExcelProperty("员工姓名")
    private String employeeName;
    @ExcelProperty("特定绩效考核依据(文件名.pdf）")
    private String fileName;
    @ExcelProperty("金额")
    private BigDecimal reward;
    @ExcelProperty("员工岗位")
    private String positionName;
    @ExcelProperty("所属部门")
    private String dept;
    @ExcelProperty("错误原因")
    private String error;
}
