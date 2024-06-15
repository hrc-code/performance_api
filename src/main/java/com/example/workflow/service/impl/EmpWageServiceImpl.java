package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.EmpWageMapper;
import com.example.workflow.model.entity.EmpWage;
import com.example.workflow.service.EmpWageService;
import org.springframework.stereotype.Service;

@Service
public class EmpWageServiceImpl extends ServiceImpl<EmpWageMapper, EmpWage> implements EmpWageService {
}
