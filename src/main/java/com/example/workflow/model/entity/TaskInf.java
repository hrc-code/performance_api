package com.example.workflow.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class TaskInf extends TaskView{
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private String position;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    private String empName;
}
