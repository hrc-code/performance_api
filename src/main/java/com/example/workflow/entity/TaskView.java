package com.example.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class TaskView {

    @TableField(value = "PROC_INST_ID_")
    private String procInstId;
    @TableField(value = "NAME_")
    private String name;
    @TableField(value = "ASSIGNEE_")
    private String assignee;
    @TableField(value = "SUSPENSION_STATE_")
    private Integer suspensionState;
    @TableField(value = "START_USER_ID_")
    private String startUserId;
    private String empName;
    @TableField(value = "STATE_")
    private String state;
    @TableField(value = "id")
    private String id;
}
