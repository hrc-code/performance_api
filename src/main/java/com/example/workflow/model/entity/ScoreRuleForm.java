package com.example.workflow.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ScoreRuleForm {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long scoreId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private BigDecimal percent;
    private String ins;
    private Boolean state;
    private List<ScoreAssessors> personList;
}
