package com.example.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workflow.entity.DeptHierarchy;
import org.apache.ibatis.annotations.Mapper;

/**
* @author hrc
* @description 针对表【dept_hierarchy(部门等级表)】的数据库操作Mapper
* @createDate 2024-03-27 23:22:48
* @Entity com.example.workflow.entity.DeptHierarchy
*/
@Mapper
public interface DeptHierarchyMapper extends BaseMapper<DeptHierarchy> {

}




