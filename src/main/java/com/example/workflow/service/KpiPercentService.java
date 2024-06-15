package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.KpiPercent;
import com.example.workflow.model.entity.KpiRuleForm;

import java.util.List;

public interface KpiPercentService extends IService<KpiPercent> {
    List<KpiPercent> splitForm(KpiRuleForm form, Long kpiId);
}
