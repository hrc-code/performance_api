package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.PositionScore;
import com.example.workflow.entity.ScoreAssessors;
import com.example.workflow.entity.ScoreRule;
import com.example.workflow.entity.ScoreRuleForm;
import com.example.workflow.mapper.ScoreRuleMapper;
import com.example.workflow.service.PositionScoreService;
import com.example.workflow.service.ScoreAssessorsService;
import com.example.workflow.service.ScoreRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScoreRuleServiceImpl extends ServiceImpl<ScoreRuleMapper, ScoreRule> implements ScoreRuleService {
    @Autowired
    private ScoreRuleService ScoreRuleService;
    @Autowired
    private PositionScoreService PositionScoreService;
    @Autowired
    private ScoreAssessorsService ScoreAssessorsService;


    @Override
    public void monthCopy(){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()).minusMonths(1), LocalTime.MAX);

        List<ScoreRule> list=ScoreRuleService.lambdaQuery()
                .eq(ScoreRule::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        list.forEach(x->{

            List<PositionScore> positionScores= PositionScoreService.lambdaQuery()
                    .eq(PositionScore::getScoreId,x.getId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();

            x.setId(null);
            ScoreRuleService.save(x);

            positionScores.forEach(y->{
                List<ScoreAssessors> scoreAssessors= ScoreAssessorsService.lambdaQuery()
                        .eq(ScoreAssessors::getPositionScoreId,y.getId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .list();

                y.setId(null);
                y.setScoreId(x.getId());
                PositionScoreService.save(y);

                scoreAssessors.forEach(z->{
                    z.setId(null);
                    z.setPositionScoreId(y.getId());
                    ScoreAssessorsService.save(z);
                });
            });
        });
    }

    @Override
    public PositionScore splitForm(ScoreRuleForm form){
        PositionScore positionScore=new PositionScore();
        positionScore.setId(form.getId());
        positionScore.setScoreId(form.getScoreId());
        positionScore.setPercent(form.getPercent());
        positionScore.setPositionId(form.getPositionId());
        positionScore.setState(form.getState()? (short) 1 : (short) 0);

        return positionScore;
    }


}
