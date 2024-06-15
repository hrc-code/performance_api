package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.OkrForm;
import com.example.workflow.model.entity.OkrRule;

public interface OkrRuleService extends IService<OkrRule> {
    OkrRule split(OkrForm form);
}
