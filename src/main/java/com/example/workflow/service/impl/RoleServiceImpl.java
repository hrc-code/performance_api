package com.example.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.dto.RoleFormDto;
import com.example.workflow.entity.Role;
import com.example.workflow.entity.RoleBtn;
import com.example.workflow.entity.RoleRouter;
import com.example.workflow.mapper.RoleBtnMapper;
import com.example.workflow.mapper.RoleMapper;
import com.example.workflow.mapper.RoleRouterMapper;
import com.example.workflow.service.RoleService;
import com.example.workflow.vo.MenuVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 角色信息 服务实现类
 * </p>
 *
 * @author 黄历
 * @since 2024-03-12
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleBtnMapper roleBtnMapper;
    private final RoleRouterMapper roleRouterMapper;

    @Override
    public void addRole(RoleFormDto roleFormDtoList) {
        Role role = BeanUtil.copyProperties(roleFormDtoList,Role.class);
        save(role);
        Long roleId = role.getId();

        List<MenuVo> menuVoList = roleFormDtoList.getMenu();
        List<RoleBtn> roleBtnList = new ArrayList<>();
        List<RoleRouter> roleRouterList = new ArrayList<>();

        for (MenuVo menuVo : menuVoList) {
            if(menuVo.getType() == 3) {
                RoleBtn roleBtn = new RoleBtn();

                roleBtn.setRoleId(roleId);
                roleBtn.setBtnId(Long.valueOf(menuVo.getId()));

                roleBtnList.add(roleBtn);
            }else {
                RoleRouter roleRouter = new RoleRouter();

                roleRouter.setRoleId(roleId);
                roleRouter.setRouterId(menuVo.getId());

                roleRouterList.add(roleRouter);
            }
        }

        Db.saveBatch(roleBtnList);
        Db.saveBatch(roleRouterList);
    }

    @Override
    public void updateRole(RoleFormDto roleFormDto) {
        Role role = BeanUtil.copyProperties(roleFormDto,Role.class);
        role.setUpdateTime(LocalDateTime.now());
        updateById(role);

        List<RoleBtn> roleBtnList = new ArrayList<>();
        List<RoleRouter> roleRouterList = new ArrayList<>();

        for (MenuVo menuVo : roleFormDto.getMenu()) {
            if(menuVo.getType() == 3) {
                RoleBtn roleBtn = new RoleBtn();
                roleBtn.setBtnId(Long.valueOf(menuVo.getId()));
                roleBtn.setRoleId(roleFormDto.getId());
                roleBtnList.add(roleBtn);
            }else {
                RoleRouter roleRouter = new RoleRouter();
                roleRouter.setRoleId(roleFormDto.getId());
                roleRouter.setRouterId(menuVo.getId());
                roleRouterList.add(roleRouter);
            }
        }

        QueryWrapper<RoleRouter>  roleRouterQueryWrapper = new QueryWrapper<>();
        roleRouterQueryWrapper.eq("role_id",roleFormDto.getId());
        roleRouterMapper.delete(roleRouterQueryWrapper);

        Db.saveBatch(roleRouterList);


        QueryWrapper<RoleBtn>  roleBtnQueryWrapper = new QueryWrapper<>();
        roleBtnQueryWrapper.eq("role_id",roleFormDto.getId());
        roleBtnMapper.delete(roleBtnQueryWrapper);

        Db.saveBatch(roleBtnList);
    }
}
