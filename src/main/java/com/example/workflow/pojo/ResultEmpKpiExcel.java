package com.example.workflow.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ResultEmpKpiExcel {
    private String empName;
    private BigDecimal inTarget1;
    private BigDecimal inTarget2;
    private BigDecimal result;
    private BigDecimal correctedValue;
    private String name;
    private String target1;
    private String target2;
}
