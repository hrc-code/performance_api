package com.example.workflow.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PieceRuleView{
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private String name;
    private String target1;
    private String input1Style;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long input1Person;
    private BigDecimal targetNum;
    private String target2;
    private String input2Style;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long input2Person;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessorId;
    private Short state;
    private String ins;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;
    private String assessorName;
}
