package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.PositionAssessor;

public interface PositionAssessorService extends IService<PositionAssessor> {

    void monthCopy();
}
