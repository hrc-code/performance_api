package com.example.workflow.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmpScoreForm extends EmpScore{
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessorId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empPositionId;
    private BigDecimal score;
}
