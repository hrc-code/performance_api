package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.*;
import com.example.workflow.feedback.EmpCoefficientError;
import com.example.workflow.feedback.EmpPieceError;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.pojo.EmpCoefficientExcel;
import com.example.workflow.pojo.EmpPieceExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmpCoefficientExcelReadListener implements ReadListener<EmpCoefficientExcel> {
    private static final int BATCH_COUNT = 20;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
    private List<EmpCoefficientExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    public EmpCoefficientExcelReadListener() {

    }
    @Override
    public void invoke(EmpCoefficientExcel data, AnalysisContext context) {
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
        List<EmpCoefficientError> errorList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(empCoefficientExcel -> {
                Employee employee = Db.lambdaQuery(Employee.class)
                        .eq(Employee::getNum,empCoefficientExcel.getNum()).one();

                EmpCoefficientError error=new EmpCoefficientError();
                if(employee==null){
                    BeanUtils.copyProperties(empCoefficientExcel, error);
                    error.setError("员工不存在");
                    errorList.add(error);
                    return;
                }

                EmpCoefficient empCoefficient=Db.lambdaQuery(EmpCoefficient.class)
                        .eq(EmpCoefficient::getEmpId,employee.getId())
                        .one();

                if(!empCoefficientExcel.getRegion().isEmpty()){
                    RegionCoefficient region=Db.lambdaQuery(RegionCoefficient.class)
                            .eq(RegionCoefficient::getRegion,empCoefficientExcel.getRegion())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                            .eq(RegionCoefficient::getState,1)
                            .one();
                    if(region==null){
                        BeanUtils.copyProperties(empCoefficientExcel, error);
                        error.setError("服务地区不存在");
                        errorList.add(error);
                        return;
                    }
                    else {
                        empCoefficient.setRegionCoefficientId(region.getId());
                    }
                }

                if(empCoefficientExcel.getPositionCoefficient()!=null){
                    empCoefficient.setPositionCoefficient(empCoefficientExcel.getPositionCoefficient());
                }

                if(empCoefficientExcel.getWage()!=null){
                    empCoefficient.setWage(empCoefficientExcel.getWage());
                }

                if(empCoefficientExcel.getPerformanceWage()!=null){
                    empCoefficient.setPerformanceWage(empCoefficientExcel.getPerformanceWage());
                }

                if (Check.noNull(empCoefficientExcel.getNum())){
                    Db.updateById(empCoefficient);
                }
            });
        }
        ErrorExcelWrite.setErrorCollection(errorList);
    }
}
