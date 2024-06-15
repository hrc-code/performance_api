package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.OkrRuleMapper;
import com.example.workflow.model.entity.OkrForm;
import com.example.workflow.model.entity.OkrRule;
import com.example.workflow.service.OkrRuleService;
import org.springframework.stereotype.Service;

@Service
public class OkrRuleServiceImpl extends ServiceImpl<OkrRuleMapper, OkrRule> implements OkrRuleService {

    @Override
    public OkrRule split(OkrForm form){
        OkrRule one=new OkrRule();
        one.setTarget(form.getTarget());
        one.setAssessorId(form.getAssessorId());
        one.setTotalScore(form.getTotalScore());
        one.setIns(form.getIns());

        return one;
    }
}
