package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.ScoreAssessors;
import com.example.workflow.model.entity.ScoreRuleForm;

import java.util.List;

public interface ScoreAssessorsService extends IService<ScoreAssessors> {
    List<ScoreAssessors> splitForm(ScoreRuleForm form, Long rule_id);
}
