package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.EmpPiece;
import com.example.workflow.entity.EmpPositionView;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.PieceRule;
import com.example.workflow.entity.PositionPiece;
import com.example.workflow.feedback.EmpPieceError;
import com.example.workflow.feedback.ErrorExcelWrite;
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
                        .eq(Employee::getNum, num)
                        .eq(Employee::getName,empPieceExcel.getEmployeeName())
                        .one();;

                EmpPieceError error=new EmpPieceError();
                if(employee==null){
                    BeanUtils.copyProperties(empPieceExcel, error);
                    error.setError("员工不存在");
                    errorList.add(error);
                    return;
                }

                EmpPositionView emp=Db.lambdaQuery(EmpPositionView.class)
                        .eq(EmpPositionView::getPosition,empPieceExcel.getPositionName())
                        .eq(EmpPositionView::getDeptName,empPieceExcel.getDept())
                        .eq(EmpPositionView::getEmpName,empPieceExcel.getEmployeeName())
                        .eq(EmpPositionView::getState,1)
                        .one();
                if(emp==null){
                    BeanUtils.copyProperties(empPieceExcel, error);
                    error.setError("责任人不存在/责任人不在该岗位下/该岗位不在该部门下");
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

                PositionPiece positionKpi=Db.lambdaQuery(PositionPiece.class)
                        .eq(PositionPiece::getPositionId,emp.getPositionId())
                        .eq(PositionPiece::getPieceId,piece.getId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one();
                if(positionKpi==null){
                    BeanUtils.copyProperties(empPieceExcel, error);
                    error.setError("该计件条目不在该岗位下");
                    errorList.add(error);
                    return;
                }

                if (Check.noNull(empPieceExcel.getQuality(),
                        empPieceExcel.getQuantity(),employee,num,emp,piece,positionKpi)) {
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
