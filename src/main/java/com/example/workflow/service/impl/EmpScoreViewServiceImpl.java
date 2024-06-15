package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.EmpScoreViewMapper;
import com.example.workflow.model.entity.EmpScoreView;
import com.example.workflow.service.EmpScoreViewService;
import org.springframework.stereotype.Service;

@Service
public class EmpScoreViewServiceImpl extends ServiceImpl<EmpScoreViewMapper, EmpScoreView> implements EmpScoreViewService {
}
