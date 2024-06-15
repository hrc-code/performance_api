package com.example.workflow.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScoreContactAssessors {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long scoreId;
    private String ins;
    private Short state;
    private String target;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionScoreId;
    private Float scorePercent;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long scoreAssessorsId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessorId;
    private String assessorName;
    private Float assessorPercent;
    private Short assessorState;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;
}
