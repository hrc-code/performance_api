package com.example.workflow.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmpPieceView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empPieceId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long pieceId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private Integer workOrder;
    private BigDecimal quality;
    private String name;
    private BigDecimal targetNum;
    private String empName;
    private Short state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime createTime;
}
