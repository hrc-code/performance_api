package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.EmpCoefficient;

public interface EmpCoefficientService extends IService<EmpCoefficient> {
    void monthCopy();
}
