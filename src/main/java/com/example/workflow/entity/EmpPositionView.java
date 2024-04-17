package com.example.workflow.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmpPositionView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private String position;
    private String empName;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    private BigDecimal posiPercent;
    private Short state;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long deptId;
    private String deptName;
    private Short type;
    private String typeName;
    private Short auditStatus;
    private String processKey;
    private String processDefinitionId;
}
