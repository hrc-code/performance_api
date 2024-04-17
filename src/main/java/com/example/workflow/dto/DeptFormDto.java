package com.example.workflow.dto;

import lombok.Data;

@Data
public class DeptFormDto {
    /**
     * 父部门id
     */
    private Long parentId;
    /**
     * 父部门等级
     */
    private Integer parentLevel;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 备注
     */
    private String remark;

    /**
     * 部门级别
     */
    private Integer level;


}
