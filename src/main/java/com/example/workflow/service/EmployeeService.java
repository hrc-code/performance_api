package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.common.R;
import com.example.workflow.dto.EmployeeFormDto;
import com.example.workflow.entity.Employee;

import java.util.List;

/**
* @author hrc
* @description 针对表【employee(员工表)】的数据库操作Service
* @createDate 2024-03-28 10:27:23
*/
public interface EmployeeService extends IService<Employee> {
    R allByCeoId(Long ceoId);
    R getInfoById(Long id);

    R infoById(Long id);
    R page(EmployeeFormDto employeeFormDto, Page<Employee> page);
    R addEmployee(EmployeeFormDto employee);

    R getEmployeeById(List<Long> ids);

    R updateEmployee(EmployeeFormDto employeeFormDto) throws Exception;

    R deleteEmployeeById(List<Long> ids);

    R lookByLike(EmployeeFormDto employeeFormDto);

    R router(Long id);
}
