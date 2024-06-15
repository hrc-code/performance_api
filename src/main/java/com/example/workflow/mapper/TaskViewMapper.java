package com.example.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workflow.model.entity.TaskView;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskViewMapper extends BaseMapper<TaskView> {
}
