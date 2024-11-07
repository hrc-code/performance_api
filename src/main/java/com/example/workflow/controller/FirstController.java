package com.example.workflow.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.model.entity.EmpCoefficient;
import com.example.workflow.model.entity.KpiRule;
import com.example.workflow.model.entity.PieceRule;
import com.example.workflow.model.entity.RegionCoefficient;
import com.example.workflow.model.entity.ScoreRule;
import com.example.workflow.model.vo.PositionAssessorView;
import com.example.workflow.service.EmpCoefficientService;
import com.example.workflow.service.KpiRuleService;
import com.example.workflow.service.PieceRuleService;
import com.example.workflow.service.PositionAssessorViewService;
import com.example.workflow.service.RegionCoefficientService;
import com.example.workflow.service.ScoreRuleService;
import com.example.workflow.utils.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/first")
public class FirstController {
    @Resource
    private ScoreRuleService scoreRuleService;
    @Resource
    private PieceRuleService pieceRuleService;
    @Resource
    private KpiRuleService kpiRuleService;
    @Resource
    private RegionCoefficientService regionCoefficientService;
    @Resource
    private EmpCoefficientService empCoefficientService;
    @Resource
    private PositionAssessorViewService positionAssessorViewService;


    @PostMapping("/check")
    public R<Map<String, Integer>> checkScore() {
        LocalDateTime[] time = DateTimeUtils.getTheStartAndEndTimeOfMonth();
        Map<String, Integer> map = new HashMap<>(10);
        List<ScoreRule> list = scoreRuleService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(time[0]),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[0])
                .apply(StringUtils.checkValNotNull(time[1]),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[1])
                .list();
        if (list.isEmpty()) {
            map.put("scoreRule",0);
        } else {
            map.put("scoreRule",1);
        }

        List<PieceRule> list2 = pieceRuleService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(time[0]),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[0])
                .apply(StringUtils.checkValNotNull(time[1]),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[1])
                .list();
        if (list2.isEmpty()) {
            map.put("pieceRule",0);
        } else {
            map.put("pieceRule",1);
        }

        List<KpiRule> list3 = kpiRuleService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(time[0]),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[0])
                .apply(StringUtils.checkValNotNull(time[1]),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[1])
                .list();
        if (list3.isEmpty()) {
            map.put("kpiRule",0);
        } else {
            map.put("kpiRule",1);
        }

        List<RegionCoefficient> list4 = regionCoefficientService.lambdaQuery()
                .eq(RegionCoefficient::getState,1)
                .apply(StringUtils.checkValNotNull(time),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[0])
                .apply(StringUtils.checkValNotNull(time[1]),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[1])
                .list();
        if (list4.isEmpty()) {
            map.put("region",0);
        } else {
            map.put("region",1);
        }

        List<EmpCoefficient> list5 = empCoefficientService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(time),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[0])
                .apply(StringUtils.checkValNotNull(time[1]),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[1])
                .list();
        if (list5.isEmpty()) {
            map.put("coe",0);
        } else {
            map.put("coe",1);
        }

        List<PositionAssessorView> list6 = positionAssessorViewService.lambdaQuery()
                .eq(PositionAssessorView::getState,1)
                .apply(StringUtils.checkValNotNull(time),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[0])
                .apply(StringUtils.checkValNotNull(time[1]),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", time[1])
                .list();
        if (list6.isEmpty()) {
            map.put("ass",0);
        } else {
            map.put("ass",1);
        }

        return R.success(map);
    }
}
