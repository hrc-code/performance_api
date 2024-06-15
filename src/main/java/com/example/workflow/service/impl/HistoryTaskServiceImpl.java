package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.HistoryTaskMapper;
import com.example.workflow.model.vo.HistoryTask;
import com.example.workflow.service.HistoryTaskService;
import org.springframework.stereotype.Service;

@Service
public class HistoryTaskServiceImpl extends ServiceImpl<HistoryTaskMapper, HistoryTask> implements HistoryTaskService {
}
