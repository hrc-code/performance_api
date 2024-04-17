package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.PositionForm;

public interface PositionService extends IService<Position> {
    Position splitForm(PositionForm form);
}
