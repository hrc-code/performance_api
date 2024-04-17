package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.EmpPositionView;
import com.example.workflow.mapper.EmpPositionViewMapper;
import com.example.workflow.service.EmpPositionViewService;
import org.springframework.stereotype.Service;

@Service
public class EmpPositionViewServiceImpl extends ServiceImpl<EmpPositionViewMapper, EmpPositionView> implements EmpPositionViewService {
}
