package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.RegionCoefficient;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.feedback.RegionError;
import com.example.workflow.pojo.RegionExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegionExcelReadListener implements ReadListener<RegionExcel> {

    private static final int BATCH_COUNT = 20;
    private List<RegionExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
    public RegionExcelReadListener() {

    }
    @Override
    public void invoke(RegionExcel data, AnalysisContext context) {
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
        List<RegionError> errorList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(RegionExcel -> {
                RegionCoefficient check= Db.lambdaQuery(RegionCoefficient.class)
                        .eq(RegionCoefficient::getRegion,RegionExcel.getRegion())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one();

                RegionError error=new RegionError();
                if(check!=null){
                    BeanUtils.copyProperties(RegionExcel, error);
                    error.setError("地区重复");
                    errorList.add(error);
                    return;
                }

                if (Check.noNull(RegionExcel.getRegion(),RegionExcel.getCoefficient())) {
                    RegionCoefficient RegionRule = new RegionCoefficient();;
                    RegionRule.setRegion(RegionExcel.getRegion());
                    RegionRule.setCoefficient(RegionExcel.getCoefficient());
                    Db.save(RegionRule);
                }
            });
        }
        ErrorExcelWrite.setErrorCollection(errorList);
    }
}
