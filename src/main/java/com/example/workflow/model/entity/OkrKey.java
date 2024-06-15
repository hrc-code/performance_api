package com.example.workflow.model.entity;

import com.example.workflow.common.StateChange;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OkrKey {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long ruleId;
    private String keyResult;
    private Double keyWeight;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long liaEmpId;
    @JsonDeserialize(using = StateChange.class)
    private Short state;
    //@TableField(fill= FieldFill.INSERT)
    private LocalDateTime createTime;

    //@TableField(fill=FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    //@TableField(fill = FieldFill.INSERT)
    private Long createUser;

    //@TableField(fill = FieldFill.INSERT_UPDATE)
    private  Long updateUser;
}
