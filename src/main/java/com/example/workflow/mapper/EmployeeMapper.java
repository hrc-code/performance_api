package com.example.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workflow.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
* @author hrc
* @description 针对表【employee(员工表)】的数据库操作Mapper
* @createDate 2024-03-28 10:27:23
* @Entity com.example.workflow.entity.Employee
*/
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}




