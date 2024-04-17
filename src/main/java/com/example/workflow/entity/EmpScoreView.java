package com.example.workflow.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmpScoreView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long scoreId;
    private String target;
    private String ins;
    private Short state;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessorId;
    private String assessorName;
    private BigDecimal assessorPercent;
    private Short assessorState;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    @JsonSerialize(using= ToStringSerializer.class)
    private BigDecimal scorePercent;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empScoreId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    private String empName;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long scoreAssessorsId;
    private BigDecimal score;
    private BigDecimal correctedValue;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime createTime;
}
