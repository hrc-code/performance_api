package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.KpiPercentMapper;
import com.example.workflow.model.entity.KpiPercent;
import com.example.workflow.model.entity.KpiRuleForm;
import com.example.workflow.service.KpiPercentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KpiPercentServiceImpl extends ServiceImpl<KpiPercentMapper, KpiPercent> implements KpiPercentService {
    @Override
    public List<KpiPercent> splitForm(KpiRuleForm form, Long kpiId){
        List<KpiPercent> list=new ArrayList<>();
        form.getPercentList().forEach(x->{
            KpiPercent one=new KpiPercent();
            one.setId(x.getId());
            one.setKpiId(kpiId);
            one.setKpiKey(x.getKpiKey());
            one.setRulePercent(x.getRulePercent());
            one.setResultPercent(x.getResultPercent());
            list.add(one);
        });

        return list;
    }
}
