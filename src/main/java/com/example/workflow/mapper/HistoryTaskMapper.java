package com.example.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.workflow.vo.HistoryTask;
import org.apache.catalina.Manager;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HistoryTaskMapper extends BaseMapper<HistoryTask> {
}
