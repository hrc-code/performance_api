package com.example.workflow.listener;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.common.R;
import com.example.workflow.entity.PieceRule;
import com.example.workflow.entity.ScoreRule;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.feedback.PieceError;
import com.example.workflow.pojo.PieceExcel;
import com.example.workflow.pojo.PieceExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PieceExcelReadListener implements ReadListener<PieceExcel> {

    private static final int BATCH_COUNT = 20;
    private List<PieceExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    public PieceExcelReadListener() {

    }
    @Override
    public void invoke(PieceExcel data, AnalysisContext context) {
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
            List<PieceError> errorList=new ArrayList<>();
            cachedDataList.stream().filter(Objects::nonNull).forEach(PieceExcel -> {
                PieceRule check= Db.lambdaQuery(PieceRule.class)
                        .eq(PieceRule::getName,PieceExcel.getName())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one();

                PieceError error=new PieceError();
                if(check!=null){
                    BeanUtils.copyProperties(PieceExcel, error);
                    error.setError("命名重复");
                    errorList.add(error);
                    return;
                }

                else if(PieceExcel.getName().isEmpty()){
                    BeanUtils.copyProperties(PieceExcel, error);
                    error.setError("命名不得为空");
                    errorList.add(error);
                    return;
                }
                else if(PieceExcel.getTarget1().isEmpty()){
                    BeanUtils.copyProperties(PieceExcel, error);
                    error.setError("条目一不得为空");
                    errorList.add(error);
                    return;
                }
                else if(PieceExcel.getTarget2().isEmpty()){
                    BeanUtils.copyProperties(PieceExcel, error);
                    error.setError("条目二不得为空");
                    errorList.add(error);
                    return;
                }
                else if(PieceExcel.getTargetNum()==null){
                    BeanUtils.copyProperties(PieceExcel, error);
                    error.setError("目标值不得为空");
                    errorList.add(error);
                    return;
                }


                if (Check.noNull(PieceExcel.getName(),PieceExcel.getTarget1(),PieceExcel.getTarget2(),PieceExcel.getTargetNum())) {
                    PieceRule PieceRule = new PieceRule();;
                    PieceRule.setName(PieceExcel.getName());
                    PieceRule.setTarget1(PieceExcel.getTarget1());
                    PieceRule.setTarget2(PieceExcel.getTarget2());
                    PieceRule.setTargetNum(PieceExcel.getTargetNum());
                    PieceRule.setIns(PieceExcel.getIns());
                    Db.save(PieceRule);
                }
            });
            ErrorExcelWrite.setErrorCollection(errorList);
        }
    }
}
