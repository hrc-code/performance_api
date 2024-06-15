package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.Dept;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.PositionScore;
import com.example.workflow.entity.ScoreAssessors;
import com.example.workflow.entity.ScoreRule;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.feedback.PositionScoreError;
import com.example.workflow.pojo.PositionScoreExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
        List<PositionScoreError> errorList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(positionScoreExcel -> {
                ScoreRule score=Db.lambdaQuery(ScoreRule.class).eq(ScoreRule::getTarget,positionScoreExcel.getScore())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one();
                PositionScoreError error=new PositionScoreError();
                if(score==null){
                    BeanUtils.copyProperties(positionScoreExcel, error);
                    error.setError("评分条目不存在");
                    errorList.add(error);
                    return;
                }

                Dept dept= Db.lambdaQuery(Dept.class).eq(Dept::getDeptName,positionScoreExcel.getDept())
                        .eq(Dept::getState,1).one();
                if(dept==null){
                    BeanUtils.copyProperties(positionScoreExcel, error);
                    error.setError("部门不存在");
                    errorList.add(error);
                    return;
                }

                Position position=Db.lambdaQuery(Position.class).eq(Position::getPosition,positionScoreExcel.getPosition())
                        .eq(Position::getDeptId,dept.getId())
                        .one();
                if(position==null){
                    BeanUtils.copyProperties(positionScoreExcel, error);
                    error.setError("岗位不存在");
                    errorList.add(error);
                    return;
                }

                Employee assessor=Db.lambdaQuery(Employee.class).eq(Employee::getNum,positionScoreExcel.getNum())
                        .eq(Employee::getState,1).one();
                if(assessor==null){
                    BeanUtils.copyProperties(positionScoreExcel, error);
                    error.setError("评分人不存在");
                    errorList.add(error);
                    return;
                }

                List<PositionScore> total= Db.lambdaQuery(PositionScore.class)
                        .eq(PositionScore::getPositionId,position.getId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .list();
                BigDecimal totalScore=new BigDecimal(0);
                total.forEach(x->{
                    totalScore.add(x.getPercent());
                });
                totalScore.add(positionScoreExcel.getScorePercent());
                if(totalScore.compareTo(new BigDecimal(100)) > 0){
                    BeanUtils.copyProperties(positionScoreExcel, error);
                    error.setError("评分占比大于100");
                    errorList.add(error);
                    return;
                }

                PositionScore positionScore=new PositionScore();
                if(Check.noNull(score.getId(),dept.getId(),position.getId())){
                    positionScore=Db.lambdaQuery(PositionScore.class)
                            .eq(PositionScore::getPositionId,position.getId())
                            .eq(PositionScore::getScoreId,score.getId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                            .eq(PositionScore::getState,1).one();

                    if(positionScore==null){
                        positionScore=new PositionScore();
                        positionScore.setPositionId(position.getId());
                        positionScore.setScoreId(score.getId());
                        positionScore.setPercent(positionScoreExcel.getScorePercent());
                        Db.save(positionScore);
                    }
                }

                if (Check.noNull(assessor.getId(),positionScore.getId())){
                    ScoreAssessors scoreAssessors=new ScoreAssessors();
                    scoreAssessors.setPositionScoreId(positionScore.getId());
                    scoreAssessors.setAssessorId(assessor.getId());
                    scoreAssessors.setPercent(positionScoreExcel.getAssessorPercent());
                    Db.save(scoreAssessors);
                }
            });
        }
        ErrorExcelWrite.setErrorCollection(errorList);
    }

}
