package com.example.workflow.vo;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PositionAssessorView {
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long positionId;
    private String position;
    private Short positionType;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long deptId;
    private String deptName;
    private Short auditStatus;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long fourthAssessorId;
    private String fourthAssessorName;
    private String fourthTimer;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long thirdAssessorId;
    private String thirdAssessorName;
    private String thirdTimer;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long secondAssessorId;
    private String secondAssessorName;
    private String secondTimer;
    private Short state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
