package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.PositionPieceMapper;
import com.example.workflow.model.entity.PositionPiece;
import com.example.workflow.service.PositionPieceService;
import org.springframework.stereotype.Service;

@Service
public class PositionPieceServiceImpl extends ServiceImpl<PositionPieceMapper, PositionPiece> implements PositionPieceService {
}
