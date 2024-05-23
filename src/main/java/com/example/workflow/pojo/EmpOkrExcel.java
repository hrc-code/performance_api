package com.example.workflow.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmpOkrExcel {
    @ExcelProperty("姓名")
    private String name;
    @ExcelProperty("目标")
    private String target;
    @ExcelProperty("评分人")
    private String assessorName;
    @ExcelProperty("O分值")
    private Double totalScore;
    @ExcelProperty("子目标")
    private String keyResult;
    @ExcelProperty("KR权重")
    private Double keyWeight;
    @ExcelProperty("得分")
    private BigDecimal score;
    @ExcelProperty("修正值")
    private BigDecimal correctedValue;
}
