package com.example.workflow.entity;

import lombok.Data;

@Data
public class TaskState {
    private Integer scoreState;
    private Integer pieceState;
    private Integer kpiState;
    private Integer okrState;
}
