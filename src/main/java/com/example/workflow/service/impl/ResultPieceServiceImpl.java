package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.ResultPieceMapper;
import com.example.workflow.model.entity.ResultPiece;
import com.example.workflow.service.ResultPieceService;
import org.springframework.stereotype.Service;

@Service
public class ResultPieceServiceImpl extends ServiceImpl<ResultPieceMapper, ResultPiece> implements ResultPieceService {
}
