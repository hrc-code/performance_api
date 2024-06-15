package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.dto.RoleFormDto;
import com.example.workflow.model.entity.Role;

/**
 * <p>
 * 角色信息 服务类
 * </p>
 *
 * @author 黄历
 * @since 2024-03-12
 */
public interface RoleService extends IService<Role> {

    void addRole(RoleFormDto roleFormDtoList);

    void updateRole(RoleFormDto roleFormDto);
}
