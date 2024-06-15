package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.Position;
import com.example.workflow.model.entity.PositionForm;

public interface PositionService extends IService<Position> {
    Position splitForm(PositionForm form);
}
