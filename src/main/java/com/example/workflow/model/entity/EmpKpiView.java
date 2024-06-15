package com.example.workflow.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmpKpiView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empKpiId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    private String empName;
    private BigDecimal inTarget1;
    private BigDecimal inTarget2;
    private BigDecimal result;
    private BigDecimal correctedValue;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long kpiId;
    private String name;
    private String target1;
    private String target2;
    private Short state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime createTime;
}
