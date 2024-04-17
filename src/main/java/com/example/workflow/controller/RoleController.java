package com.example.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.bean.PageBean;
import com.example.workflow.common.R;
import com.example.workflow.dto.AccreditFormDto;
import com.example.workflow.dto.RoleFormDto;
import com.example.workflow.entity.*;
import com.example.workflow.service.RoleService;
import com.example.workflow.vo.MenuVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色信息 前端控制器
 * </p>
 *
 * @author 黄历
 * @since 2024-03-12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/role")
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/list")
    public R<PageBean> getRoleList (@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Role> page = Page.of(pageNum,pageSize);

        Page<Role> rolePage = roleService.page(page);

        PageBean pageBean = new PageBean(rolePage.getTotal(),rolePage.getRecords());

        return R.success(pageBean);
    }

    @GetMapping("/all")
    public R<List<Role>> getAllRole () {

        List<Role> roleList = roleService.list();

        return R.success(roleList);
    }

    @GetMapping("/one/{id}")
    public R<Role> getOneRole (@PathVariable Long id) {

        Role role = roleService.lambdaQuery().eq(Role::getId,id).one();

        return R.success(role);
    }

    @GetMapping("/menuIds/{id}")
    public R<List<String>> getMenuIds (@PathVariable Long id) {
        List<String> menuIds = new ArrayList<>();

        List<RoleBtn> roleBtnList = Db.lambdaQuery(RoleBtn.class).eq(RoleBtn::getRoleId,id).list();
        List<RoleRouter> roleRouterList = Db.lambdaQuery(RoleRouter.class).eq(RoleRouter::getRoleId,id).list();

        for(RoleBtn roleBtn : roleBtnList) menuIds.add(String.valueOf(roleBtn.getBtnId()));

        for(RoleRouter roleRouter : roleRouterList) menuIds.add(roleRouter.getRouterId());


        return R.success(menuIds);
    }

    @PostMapping("/add")
    public R addRole(@RequestBody RoleFormDto roleFormDto) {
        roleService.addRole(roleFormDto);
        return R.success();
    }

    /**
     * 授权用户
     * @param accreditFormDto
     */
    @PutMapping("/accredit")
    public R accreditUser(@RequestBody AccreditFormDto accreditFormDto) {
        List<Long> userIds = accreditFormDto.getUserIds();

        List<Employee> employees = new ArrayList<>();
        for(Long userId : userIds) {
            Employee employee = new Employee();
            employee.setId(userId);
            employee.setRoleId(accreditFormDto.getRoleId());

            employees.add(employee);
        }

        Db.updateBatchById(employees);

        return R.success();
    }

    @PutMapping("/update")
    public R updateRole(@RequestBody RoleFormDto roleFormDto) {
        roleService.updateRole(roleFormDto);
        return R.success();
    }

    @DeleteMapping("/delete/{ids}")
    public R deleteRole(@PathVariable List<Long> ids) {

        //是否可以根据id全部删除
        Boolean bool = true;

        List<Long> deleteIds = new ArrayList<>();

        for (Long id : ids) {
            Employee employee = Db.lambdaQuery(Employee.class).eq(Employee::getRoleId,id).last("LIMIT 1").one();
            if (employee != null) bool = false;
            else deleteIds.add(id);
        }

        if (deleteIds.size() > 0) Db.removeByIds(deleteIds,Role.class);

        if (bool) return R.success();
        else return R.error("部分角色无法删除");

    }

    @GetMapping("/menu")
    public R<List<MenuVo>> getMenu () {
        //查询按钮列表
        List<Button> buttonList = Db.lambdaQuery(Button.class).list();
        //查询路由列表
        List<Router> routerList = Db.lambdaQuery(Router.class).list();

        List<MenuVo> menuVoList = new ArrayList<>();

        Map<String,MenuVo> menuVoMap = new HashMap<>();

        for(Router item : routerList) {
            MenuVo menuVo = new MenuVo();
            menuVo.setChildren(new ArrayList<>());
            menuVo.setId(item.getId());
            menuVo.setLabel(item.getName());

            if(!menuVoMap.containsKey(String.valueOf(item.getParentId()))) {
                menuVo.setType((short) 1);
                menuVoList.add(menuVo);
            }else {
                menuVo.setType((short) 2);
                menuVoMap.get(String.valueOf(item.getParentId())).getChildren().add(menuVo);
            }

            menuVoMap.put(menuVo.getId(),menuVo);
        }

        for(Button item : buttonList) {
            MenuVo menuVo = new MenuVo();
            menuVo.setChildren(new ArrayList<>());
            menuVo.setId(item.getId());
            menuVo.setLabel(item.getName());
            menuVo.setType((short) 3);

            menuVoMap.get(item.getParentRouter()).getChildren().add(menuVo);
        }

        return R.success(menuVoList);
    }

    /**
     * 检测角色名称是否重复
     * @return R
     */
    @GetMapping("/validate")
    public R validate (String roleName,Integer id) throws UnsupportedEncodingException {
        String decodedStr = URLDecoder.decode(roleName, "UTF-8");

        Role role = Db.lambdaQuery(Role.class)
                                    .eq(decodedStr != null,Role::getRoleName,decodedStr)
                                    .ne(id != null,Role::getId,id)
                                    .one();

        if (role != null) return R.error("角色名称重复");
        return R.success();
    }
}
