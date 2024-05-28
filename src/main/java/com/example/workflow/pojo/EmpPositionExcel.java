package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.math.BigDecimal;

@Data
public class EmpPositionExcel {
    @ExcelProperty("序号")
    private Integer serialNum;
    @ExcelProperty("员工")
    private String empName;
    @ExcelProperty("员工工号")
    private String empNum;
    @ExcelProperty("所属部门（请以，划分）")
    protected String dept;
    @ExcelProperty("岗位（请以，划分）")
    private String position;
    @ExcelProperty("岗位占比（请以，划分）")
    private String posiPercent;
}
