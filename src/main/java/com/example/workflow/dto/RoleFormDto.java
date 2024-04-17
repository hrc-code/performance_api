package com.example.workflow.dto;

import com.example.workflow.vo.MenuVo;
import lombok.Data;

import java.util.List;

@Data
public class RoleFormDto {
    /**
     * 角色id
     */
    private Long id;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 角色状态
     */
    private Integer state;
    /**
     * 备注
     */
    private String remark;
    /**
     * 菜单列表
     */
    private List<MenuVo> menu;
}
