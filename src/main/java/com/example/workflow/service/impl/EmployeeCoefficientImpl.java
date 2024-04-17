package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.EmpCoefficient;
import com.example.workflow.mapper.EmployeeCoefficientMapper;
import com.example.workflow.service.EmployeeCoefficientService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeCoefficientImpl extends ServiceImpl<EmployeeCoefficientMapper, EmpCoefficient> implements EmployeeCoefficientService {
}
