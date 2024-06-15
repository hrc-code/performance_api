package com.example.workflow.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActReDeployment {
    private String id_;
    private String name_;
    private LocalDateTime deployTime_;
    private String source_;
    private String tenantId_;
}
