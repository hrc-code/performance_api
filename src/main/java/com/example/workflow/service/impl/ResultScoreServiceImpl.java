package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.ResultScoreMapper;
import com.example.workflow.model.entity.ResultScore;
import com.example.workflow.service.ResultScoreService;
import org.springframework.stereotype.Service;

@Service
public class ResultScoreServiceImpl extends ServiceImpl<ResultScoreMapper, ResultScore> implements ResultScoreService {
}
