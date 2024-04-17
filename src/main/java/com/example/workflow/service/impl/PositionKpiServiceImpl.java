package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.PositionKpi;
import com.example.workflow.mapper.PositionKpiMapper;
import com.example.workflow.service.PositionKpiSerivce;
import org.springframework.stereotype.Service;

@Service
public class PositionKpiServiceImpl extends ServiceImpl<PositionKpiMapper, PositionKpi> implements PositionKpiSerivce {
}
