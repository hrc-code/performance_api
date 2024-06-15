package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.EmpScore;
import com.example.workflow.model.entity.EmpScoreView;

public interface EmpScoreService extends IService<EmpScore> {
    void reChange(Long empId, Long empScoreId);

    EmpScore changeState (EmpScoreView empScoreView);
}
