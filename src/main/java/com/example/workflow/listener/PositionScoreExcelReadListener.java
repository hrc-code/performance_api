package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.*;
import com.example.workflow.pojo.PositionScoreExcel;
import com.example.workflow.pojo.ScoreExcel;
import com.example.workflow.utils.Check;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class PositionScoreExcelReadListener implements ReadListener<PositionScoreExcel> {

    private static final int BATCH_COUNT = 20;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
    private List<PositionScoreExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    public PositionScoreExcelReadListener() {

    }
    @Override
    public void invoke(PositionScoreExcel data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();

            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
    }

    private void saveData() {
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(positionScoreExcel -> {
                Long scoreId=Db.lambdaQuery(ScoreRule.class).eq(ScoreRule::getTarget,positionScoreExcel.getScore())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one().getId();

                Long deptId= Db.lambdaQuery(Dept.class).eq(Dept::getDeptName,positionScoreExcel.getDept())
                        .eq(Dept::getState,1).one().getId();

                Long positionId=Db.lambdaQuery(Position.class).eq(Position::getPosition,positionScoreExcel.getPosition())
                        .eq(Position::getDeptId,deptId)
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one().getId();

                PositionScore positionScore=new PositionScore();
                if(Check.noNull(scoreId,deptId,positionId)){
                    positionScore=Db.lambdaQuery(PositionScore.class)
                            .eq(PositionScore::getPositionId,positionId)
                            .eq(PositionScore::getScoreId,scoreId)
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                            .eq(PositionScore::getState,1).one();

                    if(positionScore==null){
                        positionScore.setPositionId(positionId);
                        positionScore.setScoreId(scoreId);
                        positionScore.setPercent(positionScoreExcel.getScorePercent());
                        Db.save(positionScore);
                    }
                }

                Long assessorId=Db.lambdaQuery(Employee.class).eq(Employee::getNum,positionScoreExcel.getNum())
                        .eq(Employee::getState,1).one().getId();

                if (Check.noNull(assessorId,positionScore.getId())){
                    ScoreAssessors scoreAssessors=new ScoreAssessors();
                    scoreAssessors.setPositionScoreId(positionScore.getId());
                    scoreAssessors.setAssessorId(assessorId);
                    scoreAssessors.setPercent(positionScoreExcel.getAssessorPercent());
                    Db.save(scoreAssessors);
                }
            });
        }
    }

}
