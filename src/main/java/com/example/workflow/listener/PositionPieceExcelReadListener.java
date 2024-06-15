package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.Dept;
import com.example.workflow.entity.PieceRule;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.PositionPiece;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.feedback.PositionPieceError;
import com.example.workflow.pojo.PositionPieceExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
        List<PositionPieceError> errorList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(positionPieceExcel -> {
                PieceRule piece= Db.lambdaQuery(PieceRule.class).eq(PieceRule::getName,positionPieceExcel.getPiece())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one();

                PositionPieceError error=new PositionPieceError();
                if(piece==null){
                    BeanUtils.copyProperties(positionPieceExcel, error);
                    error.setError("缺少对应的计件条目");
                    errorList.add(error);
                    return;
                }

                Dept dept= Db.lambdaQuery(Dept.class).eq(Dept::getDeptName,positionPieceExcel.getDept())
                        .eq(Dept::getState,1).one();
                if(dept==null){
                    BeanUtils.copyProperties(positionPieceExcel, error);
                    error.setError("该员工所属部门错误");
                    errorList.add(error);
                    return;
                }

                Position position=Db.lambdaQuery(Position.class).eq(Position::getPosition,positionPieceExcel.getPosition())
                        .eq(Position::getDeptId,dept.getId())
                        .one();
                if(position==null){
                    BeanUtils.copyProperties(positionPieceExcel, error);
                    error.setError("该员工所属岗位错误");
                    errorList.add(error);
                    return;
                }

                PositionPiece positionPiece=new PositionPiece();
                if(Check.noNull(piece.getId(),dept.getId(),position.getId())){
                    positionPiece.setPositionId(position.getId());
                    positionPiece.setPieceId(piece.getId());
                    Db.save(positionPiece);
                }
            });
        }
        ErrorExcelWrite.setErrorCollection(errorList);
    }
}
