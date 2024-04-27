package com.example.workflow.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KpiRulePercent {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String name;
    private String target1;
    private String target2;
    private Integer type;
    private Short state;
    private String ins;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long kpiKey;
    private Float rulePercent;
    private Float resultPercent;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
