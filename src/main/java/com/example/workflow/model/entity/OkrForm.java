package com.example.workflow.model.entity;

import com.example.workflow.common.StateChange;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class OkrForm {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String target;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessorId;
    private Double totalScore;
    @JsonDeserialize(using = StateChange.class)
    private Short state;
    private String ins;
    private List<OkrKey> keyList;
}
