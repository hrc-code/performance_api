package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.EmpPiece;
import com.example.workflow.model.entity.EmpPieceView;

public interface EmpPieceService extends IService<EmpPiece> {
    void reChange(Long empPieceId, Long empId);

    EmpPiece changeState(EmpPieceView empPieceView);
}
