package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.ScoreRule;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.feedback.ScoreError;
import com.example.workflow.pojo.ScoreExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScoreExcelReadListener implements ReadListener<ScoreExcel> {

    private static final int BATCH_COUNT = 20;
    private List<ScoreExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
    public ScoreExcelReadListener() {

    }
    @Override
    public void invoke(ScoreExcel data, AnalysisContext context) {
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
            List<ScoreError> errorList=new ArrayList<>();
            cachedDataList.stream().filter(Objects::nonNull).forEach(scoreExcel -> {
                ScoreRule check= Db.lambdaQuery(ScoreRule.class)
                        .eq(ScoreRule::getTarget,scoreExcel.getTarget())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one();

                ScoreError error=new ScoreError();
                if(check!=null){
                    BeanUtils.copyProperties(scoreExcel, error);
                    error.setError("命名重复");
                    errorList.add(error);
                    return;
                }
                else if(scoreExcel.getTarget().isEmpty()){
                    BeanUtils.copyProperties(scoreExcel, error);
                    error.setError("命名不得为空");
                    errorList.add(error);
                    return;
                }


                if (Check.noNull(scoreExcel.getTarget())) {
                    ScoreRule scoreRule = new ScoreRule();;
                    scoreRule.setTarget(scoreExcel.getTarget());
                    scoreRule.setIns(scoreExcel.getIns());
                    Db.save(scoreRule);
                }
            });
            ErrorExcelWrite.setErrorCollection(errorList);
        }
    }
}
