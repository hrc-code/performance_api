package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.model.entity.Dept;
import com.example.workflow.model.entity.KpiRule;
import com.example.workflow.model.entity.Position;
import com.example.workflow.model.entity.PositionKpi;
import com.example.workflow.model.feedback.ErrorExcelWrite;
import com.example.workflow.model.feedback.PositionKpiError;
import com.example.workflow.model.pojo.PositionKpiExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
        List<PositionKpiError> errorList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(positionKpiExcel -> {
                KpiRule kpi= Db.lambdaQuery(KpiRule.class).eq(KpiRule::getName,positionKpiExcel.getPiece())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one();
                PositionKpiError error=new PositionKpiError();
                if(kpi==null){
                    BeanUtils.copyProperties(positionKpiExcel, error);
                    error.setError("缺少对应的提成条目");
                    errorList.add(error);
                    return;
                }

                Dept dept= Db.lambdaQuery(Dept.class).eq(Dept::getDeptName,positionKpiExcel.getDept())
                        .eq(Dept::getState,1).one();
                if(dept==null){
                    BeanUtils.copyProperties(positionKpiExcel, error);
                    error.setError("该员工所属部门错误");
                    errorList.add(error);
                    return;
                }

                Position position=Db.lambdaQuery(Position.class).eq(Position::getPosition,positionKpiExcel.getPosition())
                        .eq(Position::getDeptId,dept.getId())
                        .one();
                if(position==null){
                    BeanUtils.copyProperties(positionKpiExcel, error);
                    error.setError("该员工所属岗位错误");
                    errorList.add(error);
                    return;
                }

                PositionKpi positionKpi=new PositionKpi();
                if(Check.noNull(kpi.getId(),dept.getId(),position.getId())){
                    positionKpi.setPositionId(position.getId());
                    positionKpi.setKpiId(kpi.getId());
                    Db.save(positionKpi);
                }
            });
        }
        ErrorExcelWrite.setErrorCollection(errorList);
    }
}
