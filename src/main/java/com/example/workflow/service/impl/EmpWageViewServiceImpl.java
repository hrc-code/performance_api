package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.EmpWageViewMapper;
import com.example.workflow.model.entity.EmpWageView;
import com.example.workflow.service.EmpWageViewService;
import org.springframework.stereotype.Service;

@Service
public class EmpWageViewServiceImpl extends ServiceImpl<EmpWageViewMapper,EmpWageView> implements EmpWageViewService {
}
