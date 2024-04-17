package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.OkrForm;
import com.example.workflow.entity.OkrRule;

public interface OkrRuleService extends IService<OkrRule> {
    OkrRule split(OkrForm form);
}
