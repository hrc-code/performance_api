package com.example.workflow.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ResultScoreEmpView{
    @JsonSerialize(using= ToStringSerializer.class)
    private Long resultScoreId;
    private Short examine;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessorId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empScoreId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    private BigDecimal score;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long scoreAssessorId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long enterId;
    private Float enterPercent;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionScoreId;
    @JsonSerialize(using= ToStringSerializer.class)
    private BigDecimal scorePercent;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long scoreId;
    private String target;
    private Short state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
