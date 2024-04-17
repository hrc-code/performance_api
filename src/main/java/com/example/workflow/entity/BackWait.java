package com.example.workflow.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BackWait {
    private Long id;
    private Long empId;
    private String type;
    private Long positionId;
    private String processKey;
    private String processDefineId;
    private String opinion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
