package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.EmpScore;

public interface EmpScoreService extends IService<EmpScore> {
    void reChange(Long empId, Long empScoreId);
}
