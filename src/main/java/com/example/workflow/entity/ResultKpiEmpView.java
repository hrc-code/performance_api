package com.example.workflow.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ResultKpiEmpView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empKpiId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private Short examine;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    private BigDecimal result;
    private BigDecimal inTarget1;
    private BigDecimal inTarget2;
    private String name;
    private String target1;
    private String target2;
    private Short state;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;
}
