package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.PositionScore;
import com.example.workflow.mapper.PositionScoreMapper;
import com.example.workflow.service.PositionScoreService;
import org.springframework.stereotype.Service;

@Service
public class PositionScoreServiceImpl extends ServiceImpl<PositionScoreMapper, PositionScore> implements PositionScoreService {
}
