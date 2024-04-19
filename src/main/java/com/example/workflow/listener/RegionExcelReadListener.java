package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.RegionCoefficient;
import com.example.workflow.pojo.RegionExcel;
import com.example.workflow.pojo.RegionExcel;
import com.example.workflow.utils.Check;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public class RegionExcelReadListener implements ReadListener<RegionExcel> {

    private static final int BATCH_COUNT = 20;
    private List<RegionExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
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
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(RegionExcel -> {
                if (Check.noNull(RegionExcel.getRegion(),RegionExcel.getCoefficient())) {
                    RegionCoefficient RegionRule = new RegionCoefficient();;
                    RegionRule.setRegion(RegionExcel.getRegion());
                    RegionRule.setCoefficient(RegionExcel.getCoefficient());
                    Db.save(RegionRule);
                }
            });
        }
    }
}
