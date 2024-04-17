package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.KpiPercent;
import com.example.workflow.entity.KpiRuleForm;

import java.util.List;

public interface KpiPercentService extends IService<KpiPercent> {
    List<KpiPercent> splitForm(KpiRuleForm form, Long kpiId);
}
