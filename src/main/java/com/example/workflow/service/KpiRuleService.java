package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.KpiRule;
import com.example.workflow.entity.KpiRuleForm;

public interface KpiRuleService extends IService<KpiRule> {

    void monthCopy();

    KpiRule splitForm(KpiRuleForm form);
}
