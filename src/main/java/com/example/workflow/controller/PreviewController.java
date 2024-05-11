package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.*;
import com.example.workflow.mapper.PositionKpiViewMapper;
import com.example.workflow.mapper.PositionPieceViewMapper;
import com.example.workflow.mapper.PositionScoreViewMapper;
import com.example.workflow.mapper.RegionCoefficientMapper;
import com.example.workflow.service.*;
import com.example.workflow.vo.PositionAssessorView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/Preview")
public class PreviewController {
    @Autowired
            private ScoreRuleService ScoreRuleService;
    @Autowired
            private PositionScoreViewMapper PositionScoreViewMapper;
    @Autowired
            private PieceRuleService PieceRuleService;
    @Autowired
            private PositionPieceViewMapper PositionPieceViewMapper;
    @Autowired
            private KpiRulePercentService KpiRulePercentService;
    @Autowired
            private PositionKpiViewMapper PositionKpiViewMapper;
    @Autowired
            private RegionCoefficientMapper RegionCoefficientMapper;
    @Autowired
            private CoefficientViewService CoefficientViewService;
    @Autowired
            private PositionAssessorViewService PositionAssessorViewService;
    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()).minusMonths(1), LocalTime.MAX);

    @PostMapping("/getScoreRulelist")
    private R<List<ScoreRule>> getScoreRulelist(){
        List<ScoreRule> list=ScoreRuleService.lambdaQuery()
                .eq(ScoreRule::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(list);
    }

    @PostMapping("/getPositionScoreList")
    private R<List<PositionScoreView>> getPositionScoreList(){

        LambdaQueryWrapper<PositionScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionScoreView::getScoreState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;;
        List<PositionScoreView> list=PositionScoreViewMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/getPieceRuleList")
    private R<List<PieceRule>> getPieceRuleList(){
        List<PieceRule> list=PieceRuleService.lambdaQuery()
                .eq(PieceRule::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(list);
    }

    @PostMapping("/getPositionPieceList")
    private R<List<PositionPieceView>> getPositionPieceList(){

        LambdaQueryWrapper<PositionPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionPieceView::getScoreState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<PositionPieceView> list=PositionPieceViewMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/getKpiRuleList")
    private R<List<KpiRulePercent>> getKpiRuleList(){

        List<KpiRulePercent> list=KpiRulePercentService.lambdaQuery()
                .eq(KpiRulePercent::getState,1)
                .orderByAsc(KpiRulePercent::getId)
                .orderByAsc(KpiRulePercent::getKpiKey)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(list);
    }

    @PostMapping("/getPositionKpiList")
    private R<List<PositionKpiView>> getPositionKpiList(){

        LambdaQueryWrapper<PositionKpiView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionKpiView::getKpiState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<PositionKpiView> list=PositionKpiViewMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/getRegionCoefficientList")
    private R<List<RegionCoefficient>> getRegionCoefficientList(){
        LambdaQueryWrapper<RegionCoefficient> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(RegionCoefficient::getId)
                .eq(RegionCoefficient::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<RegionCoefficient> list=RegionCoefficientMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/getEmpCoefficientList")
    private R<List<CoefficientView>> getEmpCoefficientList(){

        List<CoefficientView> list=CoefficientViewService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .orderByDesc(CoefficientView::getEmpId)
                .list();

        return R.success(list);
    }

    @PostMapping("/getPositionAssessorView")
    private R<List<PositionAssessorView>> getPositionAssessorView(){

        List<PositionAssessorView> list=PositionAssessorViewService.lambdaQuery()
                .eq(PositionAssessorView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(list);
    }

}
