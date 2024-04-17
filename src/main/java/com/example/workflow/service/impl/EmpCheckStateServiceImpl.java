package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.EmpCheckState;
import com.example.workflow.mapper.EmpCheckStateMapper;
import com.example.workflow.service.EmpCheckStateService;
import org.springframework.stereotype.Service;

@Service
public class EmpCheckStateServiceImpl extends ServiceImpl<EmpCheckStateMapper, EmpCheckState> implements EmpCheckStateService {
}
