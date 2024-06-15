package com.example.workflow.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmpRewardView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empRewardId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    private String empName;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private String position;
    private BigDecimal reward;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long fileId;
    private String fileName;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long declareId;
    private Short state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;
}
