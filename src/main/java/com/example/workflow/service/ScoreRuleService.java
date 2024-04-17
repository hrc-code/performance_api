package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.PositionScore;
import com.example.workflow.entity.ScoreRule;
import com.example.workflow.entity.ScoreRuleForm;

public interface ScoreRuleService extends IService<ScoreRule> {
    void monthCopy();

    public PositionScore splitForm(ScoreRuleForm form);

}
