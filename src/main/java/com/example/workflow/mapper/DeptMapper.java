package com.example.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workflow.model.entity.Dept;
import org.apache.ibatis.annotations.Mapper;

/**
* @author hrc
* @description 针对表【dept(部门)】的数据库操作Mapper
* @createDate 2024-03-27 21:39:56
* @Entity com.example.workflow.entity.Dept
*/
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {

}




