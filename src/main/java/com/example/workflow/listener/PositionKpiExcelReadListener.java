package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.*;
import com.example.workflow.pojo.PositionKpiExcel;
import com.example.workflow.utils.Check;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class PositionKpiExcelReadListener implements ReadListener<PositionKpiExcel> {

    private static final int BATCH_COUNT = 20;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
    private List<PositionKpiExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    public PositionKpiExcelReadListener() {

    }
    @Override
    public void invoke(PositionKpiExcel data, AnalysisContext context) {
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
            cachedDataList.stream().filter(Objects::nonNull).forEach(positionKpiExcel -> {
                Long kpiId= Db.lambdaQuery(PieceRule.class).eq(PieceRule::getName,positionKpiExcel.getPiece())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one().getId();

                Long deptId= Db.lambdaQuery(Dept.class).eq(Dept::getDeptName,positionKpiExcel.getDept())
                        .eq(Dept::getState,1).one().getId();

                Long positionId=Db.lambdaQuery(Position.class).eq(Position::getPosition,positionKpiExcel.getPosition())
                        .eq(Position::getDeptId,deptId)
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one().getId();

                PositionKpi positionKpi=new PositionKpi();
                if(Check.noNull(kpiId,deptId,positionId)){
                    positionKpi.setPositionId(positionId);
                    positionKpi.setKpiId(kpiId);
                    Db.save(positionKpi);
                }
            });
        }
    }
}
