package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.ResultKpi;
import com.example.workflow.mapper.ResultKpiMapper;
import com.example.workflow.service.ResultKpiService;
import org.springframework.stereotype.Service;

@Service
public class ResultKpiServiceImpl extends ServiceImpl<ResultKpiMapper, ResultKpi> implements ResultKpiService {
}
