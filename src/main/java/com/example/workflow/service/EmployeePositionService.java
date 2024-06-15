package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.PositionForm;

import java.util.List;

public interface EmployeePositionService extends IService<EmployeePosition> {
    List<EmployeePosition> splitForm(PositionForm form, Long id);
}
