package com.example.workflow.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class KpiRuleForm {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String name;
    private String target1;
    private String target2;
    private Integer type;
    private List<KpiPercent> percentList;
    private String ins;
}
