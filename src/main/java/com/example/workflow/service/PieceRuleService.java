package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.PieceRule;

public interface PieceRuleService extends IService<PieceRule> {
    void monthCopy();
}
