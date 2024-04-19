package com.example.workflow.listener;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.common.R;
import com.example.workflow.entity.Dept;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.ScoreRule;
import com.example.workflow.pojo.PositionExcel;
import com.example.workflow.pojo.ScoreExcel;
import com.example.workflow.utils.Check;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ScoreExcelReadListener implements ReadListener<ScoreExcel> {

    private static final int BATCH_COUNT = 20;
    private List<ScoreExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
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
            cachedDataList.stream().filter(Objects::nonNull).forEach(scoreExcel -> {
                if (Check.noNull(scoreExcel.getTarget())) {
                    ScoreRule scoreRule = new ScoreRule();;
                    scoreRule.setTarget(scoreExcel.getTarget());
                    scoreRule.setIns(scoreExcel.getIns());
                    Db.save(scoreRule);
                }
            });
        }
    }
}
