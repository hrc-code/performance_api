package com.example.workflow.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.EmpCoefficient;
import com.example.workflow.entity.KpiRule;
import com.example.workflow.entity.PieceRule;
import com.example.workflow.entity.RegionCoefficient;
import com.example.workflow.entity.ScoreRule;
import com.example.workflow.service.EmpCoefficientService;
import com.example.workflow.service.KpiRuleService;
import com.example.workflow.service.PieceRuleService;
import com.example.workflow.service.PositionAssessorViewService;
import com.example.workflow.service.RegionCoefficientService;
import com.example.workflow.service.ScoreRuleService;
import com.example.workflow.vo.PositionAssessorView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/first")
public class FirstController {

    @Autowired
    private ScoreRuleService ScoreRuleService;
    @Autowired
            private PieceRuleService PieceRuleService;
    @Autowired
            private KpiRuleService KpiRuleService;
    @Autowired
            private RegionCoefficientService RegionCoefficientService;
    @Autowired
            private EmpCoefficientService EmpCoefficientService;
    @Autowired
            private PositionAssessorViewService PositionAssessorViewService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @PostMapping("/check")
    private R<Map<String,Integer>> checkScore(){
        Map<String,Integer> map=new HashMap<>();
        List<ScoreRule> list= ScoreRuleService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(list.isEmpty())
            map.put("scoreRule",0);
        else
            map.put("scoreRule",1);

        List<PieceRule> list2= PieceRuleService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(list2.isEmpty())
            map.put("pieceRule",0);
        else
            map.put("pieceRule",1);

        List<KpiRule> list3= KpiRuleService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(list3.isEmpty())
            map.put("kpiRule",0);
        else
            map.put("kpiRule",1);

        List<RegionCoefficient> list4=RegionCoefficientService.lambdaQuery()
                .eq(RegionCoefficient::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(list4.isEmpty())
            map.put("region",0);
        else
            map.put("region",1);

        List<EmpCoefficient> list5= EmpCoefficientService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(list5.isEmpty())
            map.put("coe",0);
        else
            map.put("coe",1);

        List<PositionAssessorView> list6=PositionAssessorViewService.lambdaQuery()
                .eq(PositionAssessorView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(list6.isEmpty())
            map.put("ass",0);
        else
            map.put("ass",1);

        return R.success(map);
    }
}
