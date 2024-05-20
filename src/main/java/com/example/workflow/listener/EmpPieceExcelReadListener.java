package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.*;
import com.example.workflow.feedback.EmpPieceError;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.pojo.EmpPieceExcel;
import com.example.workflow.pojo.KpiExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmpPieceExcelReadListener implements ReadListener<EmpPieceExcel> {
    private static final int BATCH_COUNT = 20;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
    private List<EmpPieceExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    public EmpPieceExcelReadListener() {

    }
    @Override
    public void invoke(EmpPieceExcel data, AnalysisContext context) {
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
        List<EmpPieceError> errorList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(empPieceExcel -> {
                String num = empPieceExcel.getNum();
                Employee employee = Db.lambdaQuery(Employee.class)
                        .eq(Employee::getNum, num).one();

                EmpPieceError error=new EmpPieceError();
                if(employee==null){
                    BeanUtils.copyProperties(empPieceExcel, error);
                    error.setError("员工不存在");
                    errorList.add(error);
                    return;
                }

                Dept dept=Db.lambdaQuery(Dept.class)
                        .eq(Dept::getDeptName,empPieceExcel.getDept())
                        .eq(Dept::getState,1).one();
                if(dept==null){
                    BeanUtils.copyProperties(empPieceExcel, error);
                    error.setError("员工所属部门错误");
                    errorList.add(error);
                    return;
                }

                Position position = Db.lambdaQuery(Position.class)
                        .eq(Position::getPosition, empPieceExcel.getPositionName())
                        .eq(Position::getDeptId,dept.getId()).one();
                if(position==null){
                    BeanUtils.copyProperties(empPieceExcel, error);
                    error.setError("员工不在该岗位");
                    errorList.add(error);
                    return;
                }

                PieceRule piece= Db.lambdaQuery(PieceRule.class).eq(PieceRule::getName,empPieceExcel.getPieceName())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one();
                if(piece==null){
                    BeanUtils.copyProperties(empPieceExcel, error);
                    error.setError("该计件条目不存在");
                    errorList.add(error);
                    return;
                }

                if (Check.noNull(empPieceExcel.getQuality(),
                        empPieceExcel.getQuantity(),employee,num,dept,position,piece.getId())) {
                    EmpPiece empPiece = new EmpPiece();;
                    empPiece.setEmpId(employee.getId());
                    empPiece.setPieceId(piece.getId());
                    empPiece.setQuality(empPieceExcel.getQuality());
                    empPiece.setWorkOrder(empPieceExcel.getQuantity());
                    Db.save(empPiece);
                }
            });
        }
        ErrorExcelWrite.setErrorCollection(errorList);
    }
}
