package com.example.workflow.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OkrView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long okrId;
    private String target;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessorId;
    private String assessorName;
    private Double totalScore;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long keyId;
    private String keyResult;
    private Double keyWeight;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long liaEmpId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private String name;
    private Short state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
