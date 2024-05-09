package com.example.workflow.controller;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.common.R;
import com.example.workflow.dto.EmployeeFormDto;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.RegionCoefficient;
import com.example.workflow.service.EmployeeService;
import com.example.workflow.vo.EmployeeVo;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/employee")
public class EmployeeController {
    private final EmployeeService employeeService;

    /** 通过部门id查询员工信息*/
    @GetMapping("/infoByDeptId")
    public R<List<EmployeeVo>> getInfoByDeptId(@RequestParam Long deptId) {
        List<EmployeeVo> employeeVos = employeeService.getEmployeeVoListByDeptId(deptId);
        return R.success(employeeVos);
    }

    /** 根据员工姓名获取信息， 信息可以不全*/
    @GetMapping("/infoList")
    public R<List<EmployeeVo>> getByName(String name, String num) {
        List<EmployeeVo> list = employeeService.getList(name, num);
        return R.success(list);
    }

    /*
    * 根据ceoId查询其下的全部员工
    * ceoId 即ceo的员工id*/
    @GetMapping("/all/{ceoId}")
    public R allByCeoId(@PathVariable Long ceoId) {
            if (ceoId == null) {
                return R.error("ceoId = null");
            }
        return employeeService.allByCeoId(ceoId);
    }

    @GetMapping("/all")
    public R all() {
        ArrayList<EmployeeVo> list = new ArrayList<>();
        employeeService.list().stream().filter(Objects::nonNull).forEach(employee -> {
            EmployeeVo employeeVo = new EmployeeVo();
            Long employeeId = employee.getId();
            String name = employee.getName();
            employeeVo.setName(name);
            employeeVo.setId(employeeId);
            list.add(employeeVo);
        });
        return R.success(list);
    }

    /**  传进来一个员工id,返回这个id的路由信息和按钮权限
     *
     **/
    @GetMapping("/router/{id}")
    public R router(@PathVariable Long id) {
        return employeeService.router(id);
    }

    /**
     * 新建员工*/
    @PostMapping("/add")
    public R addEmployee(@RequestBody EmployeeFormDto employee) {

        return employeeService.addEmployee(employee);
    }
    /**
     * 获取个人信息*/
    @GetMapping("/empInfo/{id}")
    public R infoById(@PathVariable Long id) {
        if (id == null) {
            return R.error("id不能为空");
        }
        return employeeService.infoById(id);
    }

    /** 获取要修改员工的个人信息*/
    @GetMapping("info/{id}")
    public R getInfoById(@PathVariable Long id) {
        if (id == null) {
            return R.error("id不能为空");
        }
        return employeeService.getInfoById(id);
    }

    /**
     * 根据员工id查询全部的信息*/
    @Deprecated
    @GetMapping("/info")
    public R info(List<Long> id) {
        if (id == null) {
            return R.error("id不能为空");
        }
        return employeeService.getEmployeeById(id);
    }

    /**
     *更新员工*/
    @PutMapping("/update")
    public R updateEmployee(@RequestBody EmployeeFormDto employeeFormDto) throws Exception{
        if (employeeFormDto == null) {
            return R.error("信息不能为空");
        }
        return employeeService.updateEmployee(employeeFormDto);
    }

    /**
     * 删除员工*/
    @DeleteMapping("/delete/{ids}")
    public R deleteEmployeeById(@PathVariable List<Long> ids) {
        if (ids == null) {
            return R.error("id 不能为空");
        }
        return employeeService.deleteEmployeeById(ids);
    }

    /**
     * 查询员工信息  模糊查询*/
    @Deprecated
    @GetMapping("/like")
    public R like(@RequestBody EmployeeFormDto employeeFormDto) {
            if (employeeFormDto == null) {
                return R.error("信息不能为空");
            }
            return employeeService.lookByLike(employeeFormDto);
    }

    /**
     * 查询全部员工信息*/
    @Deprecated
    @GetMapping("/list")
    public R page(
            String num,String name,Long deptId,Long roleId
            ,@RequestParam(defaultValue = "1")Integer pageNum
            ,@RequestParam(defaultValue = "10")Integer pageSize) {

        EmployeeFormDto employeeFormDto = new EmployeeFormDto();

        employeeFormDto.setNum(num);
        employeeFormDto.setName(name);
        employeeFormDto.setDeptId(deptId);
        employeeFormDto.setRoleId(roleId);

       return employeeService.page(employeeFormDto);
    }

    /**
     * 查询地区列表*/
    @GetMapping("/getRegion")
    public R getRegion() {

        List<RegionCoefficient> regionCoefficients = Db.lambdaQuery(RegionCoefficient.class).list();

        return R.success(regionCoefficients);
    }

    /**
     * 验证员工工号是否重复*/
    @GetMapping("/validate")
    public R validate(String num,Long id)  {

        Employee employee = Db.lambdaQuery(Employee.class)
                .eq(num != null, Employee::getNum,num)
                .ne(id != null, Employee::getId,id)
                .one();
        if (employee != null) return R.error("员工工号重复");
        return R.success();
    }

    /**
     * 验证旧密码是否正确*/
    @GetMapping("/validateOldPwd")
    public R validateOldPwd(String oldPwd,Long id)  {
        Employee employee = Db.lambdaQuery(Employee.class)
                .eq( Employee::getPassword,oldPwd)
                .eq(id != null, Employee::getId,id)
                .one();
        if (employee == null) return R.error("旧密码错误");
        return R.success();
    }

    /**
     * 修改密码*/
    @GetMapping("/changePwd")
    public R changePwd(String newPwd,Long id)  {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setPassword(newPwd);

        employeeService.updateById(employee);

        return R.success();
    }
}
