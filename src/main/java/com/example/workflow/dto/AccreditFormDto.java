package com.example.workflow.dto;

import lombok.Data;

import java.util.List;

@Data
public class AccreditFormDto {
    /**
     * 角色id
     */
    private Long roleId;
    /**
     * 要授权用户的id数组
     */
    private List<Long> userIds;
}
