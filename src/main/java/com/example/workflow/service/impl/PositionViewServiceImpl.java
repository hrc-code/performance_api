package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.PositionViewMapper;
import com.example.workflow.model.entity.PositionView;
import com.example.workflow.service.PositionViewService;
import org.springframework.stereotype.Service;

@Service
public class PositionViewServiceImpl extends ServiceImpl<PositionViewMapper, PositionView> implements PositionViewService {
}
