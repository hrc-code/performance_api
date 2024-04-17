package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.EmpOkr;

public interface EmpOkrService extends IService<EmpOkr> {
    void reChange(Long empOkrId, Long empId);
}
