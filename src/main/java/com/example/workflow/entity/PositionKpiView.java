package com.example.workflow.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PositionKpiView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionKpiId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private String position;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long kpiId;
    private String name;
    private String target1;
    private String target2;
    private Short kpiState;
    private Short assessorState;
    private String ins;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;
}
