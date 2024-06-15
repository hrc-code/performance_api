package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.model.entity.TaskView;
import com.example.workflow.service.TaskViewService;
import org.springframework.stereotype.Service;

@Service
public class TaskViewServiceImpl extends ServiceImpl<TaskViewMapper,TaskView> implements TaskViewService {
}
