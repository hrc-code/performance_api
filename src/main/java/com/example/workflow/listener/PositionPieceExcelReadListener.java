package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.*;
import com.example.workflow.pojo.PositionPieceExcel;
import com.example.workflow.pojo.PositionPieceExcel;
import com.example.workflow.utils.Check;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class PositionPieceExcelReadListener implements ReadListener<PositionPieceExcel> {

    private static final int BATCH_COUNT = 20;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
    private List<PositionPieceExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    public PositionPieceExcelReadListener() {

    }
    @Override
    public void invoke(PositionPieceExcel data, AnalysisContext context) {
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
            cachedDataList.stream().filter(Objects::nonNull).forEach(positionPieceExcel -> {
                Long pieceId= Db.lambdaQuery(PieceRule.class).eq(PieceRule::getName,positionPieceExcel.getPiece())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one().getId();

                Long deptId= Db.lambdaQuery(Dept.class).eq(Dept::getDeptName,positionPieceExcel.getDept())
                        .eq(Dept::getState,1).one().getId();

                Long positionId=Db.lambdaQuery(Position.class).eq(Position::getPosition,positionPieceExcel.getPosition())
                        .eq(Position::getDeptId,deptId)
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one().getId();

                PositionPiece positionPiece=new PositionPiece();
                if(Check.noNull(pieceId,deptId,positionId)){
                    positionPiece.setPositionId(positionId);
                    positionPiece.setPieceId(pieceId);
                    Db.save(positionPiece);
                }
            });
        }
    }
}
