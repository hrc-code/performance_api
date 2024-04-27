package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.*;
import com.example.workflow.pojo.KpiExcel;
import com.example.workflow.utils.Check;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class KpiExcelReadListener implements ReadListener<KpiExcel> {
    private static final int BATCH_COUNT = 20;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
    private List<KpiExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    public KpiExcelReadListener() {

    }
    @Override
    public void invoke(KpiExcel data, AnalysisContext context) {
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
            cachedDataList.stream().filter(Objects::nonNull).forEach(kpiExcel -> {
                KpiRule kpiRule=Db.lambdaQuery(KpiRule.class)
                            .eq(KpiRule::getName,kpiExcel.getName())
                            .eq(KpiRule::getState,1)
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                            .one();

                    if(kpiRule==null){
                        kpiRule=new KpiRule();
                        kpiRule.setName(kpiExcel.getName());
                        kpiRule.setTarget1(kpiExcel.getTarget1());
                        kpiRule.setTarget2(kpiExcel.getTarget2());
                        Db.save(kpiRule);
                    }

                    KpiPercent kpiPercent=new KpiPercent();
                    kpiPercent.setKpiId(kpiRule.getId());
                    kpiPercent.setKpiKey(kpiExcel.getKpiKey());
                    kpiPercent.setResultPercent(kpiExcel.getResultPercent());
                    kpiPercent.setRulePercent(kpiPercent.getRulePercent());
                    Db.save(kpiPercent);

            });
        }
    }
}
