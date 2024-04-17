package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.EmpCheckStateView;
import com.example.workflow.mapper.EmpCheckStateViewMapper;
import com.example.workflow.service.EmpCheckStateViewService;
import org.springframework.stereotype.Service;

@Service
public class EmpCheckStateViewServiceImpl extends ServiceImpl<EmpCheckStateViewMapper,EmpCheckStateView> implements EmpCheckStateViewService {
}
