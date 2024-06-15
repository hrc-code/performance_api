package com.example.workflow.model.entity;

import com.example.workflow.common.StateChange;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OkrRule {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String target;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessorId;
    private Double totalScore;
    @JsonDeserialize(using = StateChange.class)
    private Short state;
    private String ins;
    //@TableField(fill= FieldFill.INSERT)
    private LocalDateTime createTime;

    //@TableField(fill=FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    //@TableField(fill = FieldFill.INSERT)
    private Long createUser;

    //@TableField(fill = FieldFill.INSERT_UPDATE)
    private  Long updateUser;
}
