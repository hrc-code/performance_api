package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.KpiRule;
import com.example.workflow.model.entity.KpiRuleForm;

public interface KpiRuleService extends IService<KpiRule> {

    void monthCopy();

    KpiRule splitForm(KpiRuleForm form);
}
