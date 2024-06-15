package com.example.workflow.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskView {

    @TableField(value = "PROC_INST_ID_")
    private String procInstId;
    @TableField(value = "NAME_")
    private String name;
    @TableField(value = "ASSIGNEE_")
    private String assignee;
    private String assessorName;
    @TableField(value = "SUSPENSION_STATE_")
    private Integer suspensionState;
    @TableField(value = "START_USER_ID_")
    private String startUserId;
    private String empName;
    @TableField(value = "STATE_")
    private String state;
    @TableField(value = "id")
    private String id;
    private LocalDateTime createTime;
}
