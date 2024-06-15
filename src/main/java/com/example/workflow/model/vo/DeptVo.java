package com.example.workflow.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 三级部门
 * </p>
 *
 * @author 黄历
 * @since 2024-03-12
 */
@Data
public class DeptVo {
    /**
     * 部门ID
     */
    @JsonFormat(shape =JsonFormat.Shape.STRING )
    private Long id;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 所属部门等级
     */
    private Short level;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 更新用户
     */
    private String updateUser;
    /**
     * 创建用户
     */
    private String createUser;
    /**
     * 子部门
     */
    private List<DeptVo> children;
}
