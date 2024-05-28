package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.*;
import com.example.workflow.feedback.EmpKpiError;
import com.example.workflow.feedback.EmpPositionError;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.pojo.EmpPositionExcel;
import com.example.workflow.pojo.KpiExcel;
import com.example.workflow.service.EmployeePositionService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmpPositionExcelReadListener implements ReadListener<EmpPositionExcel> {
    private static final int BATCH_COUNT = 20;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
    private List<EmpPositionExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    public EmpPositionExcelReadListener() {

    }
    @Override
    public void invoke(EmpPositionExcel data, AnalysisContext context) {
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
        List<EmpPositionError> errorList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(empPositionExcel -> {
                Employee employee = Db.lambdaQuery(Employee.class)
                        .eq(Employee::getNum, empPositionExcel.getEmpNum())
                        .eq(Employee::getName,empPositionExcel.getEmpName())
                        .one();

                EmpPositionError error=new EmpPositionError();
                if(employee==null){
                    BeanUtils.copyProperties(empPositionExcel, error);
                    error.setError("员工不存在");
                    errorList.add(error);
                    return;
                }

                String[] positionList= empPositionExcel.getPosition().split(",");
                String[] deptList= empPositionExcel.getDept().split(",");
                String[] percentList= empPositionExcel.getPosiPercent().split(",");
                if(!(positionList.length==deptList.length)||
                        !(positionList.length==percentList.length)){
                    BeanUtils.copyProperties(empPositionExcel, error);
                    error.setError("岗位与所属部门与岗位占比未一一对应");
                    errorList.add(error);
                    return;
                }

                BigDecimal total=new BigDecimal(0);
                for(int i=0;i<positionList.length;i++){
                    PositionView positionView= Db.lambdaQuery(PositionView.class)
                            .eq(PositionView::getPosition,positionList[i])
                            .eq(PositionView::getDeptName,deptList[i])
                            .eq(PositionView::getState,1)
                            .one();
                    if(positionView==null){
                        BeanUtils.copyProperties(empPositionExcel, error);
                        error.setError("该岗位未在所属部门下");
                        errorList.add(error);
                        return;
                    }
                    total.add(new BigDecimal(percentList[i]));
                }
                if(total.compareTo(new BigDecimal(100)) > 0){
                    BeanUtils.copyProperties(empPositionExcel, error);
                    error.setError("评分占比大于100");
                    errorList.add(error);
                    return;
                }
                
                List<EmployeePosition> employeePosition= Db.lambdaQuery(EmployeePosition.class)
                        .eq(EmployeePosition::getEmpId,employee.getId())
                        .eq(EmployeePosition::getState,1)
                        .list();
                employeePosition.forEach(x->{
                    Db.removeById(x);
                });

                for(int i=0;i<positionList.length;i++){
                    PositionView positionView= Db.lambdaQuery(PositionView.class)
                            .eq(PositionView::getPosition,positionList[i])
                            .eq(PositionView::getDeptName,deptList[i])
                            .eq(PositionView::getState,1)
                            .one();

                    EmployeePosition one=new EmployeePosition();
                    one.setPositionId(positionView.getId());
                    one.setEmpId(employee.getId());
                    one.setPosiPercent(new BigDecimal(percentList[i]));

                    if(positionView.getType()==5)
                        one.setProcessKey("Process_1gzouwy");
                    else if(positionView.getType()==4)
                        one.setProcessKey("Process_1whe0gq");
                    else if(positionView.getType()==3)
                        one.setProcessKey("Process_01p7ac7");
                    Db.save(one);
                }

            });
        }
        ErrorExcelWrite.setErrorCollection(errorList);
    }
}
