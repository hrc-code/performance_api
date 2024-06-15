package com.example.workflow.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PositionPieceView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionPieceId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private String position;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long pieceId;
    private String name;
    private String target1;
    private BigDecimal targetNum;
    private String target2;
    private Short assessorState;
    private Short scoreState;
    private String ins;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;

}
