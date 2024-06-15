package com.example.workflow.model.entity;

import lombok.Data;

@Data
public class TaskState {
    private Integer scoreState;
    private Integer pieceState;
    private Integer kpiState;
    private Integer okrState;
    private Integer totalState;
}
