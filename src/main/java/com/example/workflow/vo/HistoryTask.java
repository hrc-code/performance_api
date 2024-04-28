package com.example.workflow.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoryTask {
    private String procInstId;
    private String name;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessor;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String deleteReason;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private String position;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long assessorId;
    private String assessorName;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long empId;
    private String empName;
}
