package com.example.workflow.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PositionScoreView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionScoreId;
    @JsonSerialize(using= ToStringSerializer.class)
    private  Long positionId;
    private String position;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long scoreId;
    private String target;
    private Short scoreState;
    private String ins;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;
    private BigDecimal scorePercent;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long scoreAssessorsId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessorId;
    private String assessorName;
    private BigDecimal assessorPercent;
    private Short assessorState;
}
