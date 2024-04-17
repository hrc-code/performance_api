package com.example.workflow.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ResultPieceEmpView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empPieceId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    private String target1;
    private BigDecimal targetNum;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long workOrder;
    private String name;
    private Short examine;
    private Short state;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;
}
