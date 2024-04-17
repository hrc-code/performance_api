package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.EmpKpiView;
import com.example.workflow.mapper.EmpKpiViewMapper;
import com.example.workflow.service.EmpKpiViewService;
import org.springframework.stereotype.Service;

@Service
public class EmpKpiViewServiceImpl extends ServiceImpl<EmpKpiViewMapper, EmpKpiView> implements EmpKpiViewService {
}
