package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.EmpPiece;

public interface EmpPieceService extends IService<EmpPiece> {
    void reChange(Long empPieceId, Long empId);
}
