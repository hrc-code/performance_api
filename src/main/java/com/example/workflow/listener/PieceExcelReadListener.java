package com.example.workflow.listener;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.common.R;
import com.example.workflow.entity.PieceRule;
import com.example.workflow.pojo.PieceExcel;
import com.example.workflow.pojo.PieceExcel;
import com.example.workflow.utils.Check;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class PieceExcelReadListener implements ReadListener<PieceExcel> {

    private static final int BATCH_COUNT = 20;
    private List<PieceExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
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
            cachedDataList.stream().filter(Objects::nonNull).forEach(PieceExcel -> {
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
        }
    }
}
