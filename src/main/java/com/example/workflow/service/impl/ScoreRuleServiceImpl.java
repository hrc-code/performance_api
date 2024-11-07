package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.ScoreRuleMapper;
import com.example.workflow.model.entity.PositionScore;
import com.example.workflow.model.entity.ScoreAssessors;
import com.example.workflow.model.entity.ScoreRule;
import com.example.workflow.model.entity.ScoreRuleForm;
import com.example.workflow.service.PositionScoreService;
import com.example.workflow.service.ScoreAssessorsService;
import com.example.workflow.service.ScoreRuleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScoreRuleServiceImpl extends ServiceImpl<ScoreRuleMapper, ScoreRule> implements ScoreRuleService {
    @Resource
    private ScoreAssessorsService scoreAssessorsService;
    @Resource
    private PositionScoreService positionScoreService;




    @Override
    public void monthCopy(){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()).minusMonths(1), LocalTime.MAX);

        List<ScoreRule> list= lambdaQuery()
                .eq(ScoreRule::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        list.forEach(x->{
            List<PositionScore> positionScores = positionScoreService.lambdaQuery()
                    .eq(PositionScore::getScoreId,x.getId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();

            x.setId(null);
            save(x);

            positionScores.forEach(y->{
                List<ScoreAssessors> scoreAssessors = scoreAssessorsService.lambdaQuery()
                        .eq(ScoreAssessors::getPositionScoreId,y.getId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .list();

                y.setId(null);
                y.setScoreId(x.getId());
                positionScoreService.save(y);

                scoreAssessors.forEach(z->{
                    z.setId(null);
                    z.setPositionScoreId(y.getId());
                    scoreAssessorsService.save(z);
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
