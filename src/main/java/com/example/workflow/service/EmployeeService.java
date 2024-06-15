package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.common.R;
import com.example.workflow.model.dto.EmployeeFormDto;
import com.example.workflow.model.entity.Employee;
import com.example.workflow.model.pojo.EmpIdAndStateId;
import com.example.workflow.model.pojo.EmployeeExcel;
import com.example.workflow.model.vo.EmployeeVo;

import java.util.Collection;
import java.util.List;

/**
 * @author hrc
 * @description 针对表【employee(员工表)】的数据库操作Service
 * @createDate 2024-03-28 10:27:23
 */
public interface EmployeeService extends IService<Employee> {
    void setStateToEmployee(List<EmpIdAndStateId> empIdAndStateIdList);

    Collection<EmployeeExcel> getEmployeeExcels(Long deptId, String name, String numb);

    List<EmployeeVo> getList(String name, String num,Long roleId,Long id);

    R allByCeoId(Long ceoId);

    R getInfoById(Long id);

    R infoById(Long id);

    R page(EmployeeFormDto employeeFormDto);

    R addEmployee(EmployeeFormDto employee);

    R getEmployeeById(List<Long> ids);

    R updateEmployee(EmployeeFormDto employeeFormDto) throws Exception;

    R deleteEmployeeById(List<Long> ids);

    R lookByLike(EmployeeFormDto employeeFormDto);

    R router(Long id);

    List<EmployeeVo> getEmployeeVoListByDeptId(Long deptId);
}
