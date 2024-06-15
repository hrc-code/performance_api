package com.example.workflow.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EmpWage {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    private Long positionId;
    private BigDecimal scoreWage;
    private BigDecimal pieceWage;
    private BigDecimal kpiWage;
    private BigDecimal okrWage;
    private BigDecimal rewardWage;
    private BigDecimal total;
    private Short state;
    @TableField(fill= FieldFill.INSERT)//插入时填充字段
    private LocalDateTime createTime;

    @TableField(fill=FieldFill.INSERT_UPDATE)//插入和更新时填充字段
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private  Long updateUser;
}
