package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.KpiRuleMapper;
import com.example.workflow.model.entity.KpiPercent;
import com.example.workflow.model.entity.KpiRule;
import com.example.workflow.model.entity.KpiRuleForm;
import com.example.workflow.model.entity.PositionKpi;
import com.example.workflow.service.KpiPercentService;
import com.example.workflow.service.KpiRuleService;
import com.example.workflow.service.PositionKpiSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class KpiRuleServiceImpl extends ServiceImpl<KpiRuleMapper, KpiRule> implements KpiRuleService {
    @Autowired
    private KpiRuleService KpiRuleService;
    @Autowired
    private PositionKpiSerivce PositionKpiSerivce;
    @Autowired
    private KpiPercentService KpiPercentService;

    @Override
    public void monthCopy(){

        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()).minusMonths(1), LocalTime.MAX);

        List<KpiRule> list=KpiRuleService.lambdaQuery()
                .eq(KpiRule::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        list.forEach(x->{
            List<PositionKpi> positionKpis= PositionKpiSerivce.lambdaQuery()
                    .eq(PositionKpi::getKpiId,x.getId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();

            List<KpiPercent> kpiPercents= KpiPercentService.lambdaQuery()
                    .eq(KpiPercent::getKpiId,x.getId())
                    .eq(KpiPercent::getState,1)
                    .list();

            x.setId(null);
            KpiRuleService.save(x);

            positionKpis.forEach(y->{
                y.setId(null);
                y.setKpiId(x.getId());
                PositionKpiSerivce.save(y);
            });

            kpiPercents.forEach(y->{
                y.setId(null);
                y.setKpiId(x.getId());
                KpiPercentService.save(y);
            });
        });
    }

    @Override
    public KpiRule splitForm(KpiRuleForm form){
        KpiRule one=new KpiRule();
            one.setId(form.getId());
            one.setName(form.getName());
            one.setTarget1(form.getTarget1());
            one.setTarget2(form.getTarget2());
            one.setType(form.getType());
        return one;
    }
}
