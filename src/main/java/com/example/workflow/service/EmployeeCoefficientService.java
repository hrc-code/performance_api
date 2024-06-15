package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.EmpCoefficient;

public interface EmployeeCoefficientService extends IService<EmpCoefficient> {

    void fileOne(Long empId, Long positionId);
}
