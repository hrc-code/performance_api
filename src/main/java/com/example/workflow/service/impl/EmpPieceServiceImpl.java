package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.*;
import com.example.workflow.mapper.EmpPieceMapper;
import com.example.workflow.mapper.EmpPieceViewMapper;
import com.example.workflow.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EmpPieceServiceImpl extends ServiceImpl<EmpPieceMapper, EmpPiece> implements EmpPieceService {
    @Autowired
    private EmpPieceViewService EmpPieceViewService;
    @Autowired
            private EmpPieceViewMapper EmpPieceViewMapper;
    @Autowired
            private EmpWageService EmpWageService;
    @Autowired
            private EmployeePositionService EmployeePositionService;
    @Autowired
            private CoefficientViewService CoefficientViewService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @Override
    public void reChange(Long empPieceId, Long empId){
        EmpPieceView empPieceView= EmpPieceViewService.lambdaQuery()
                .eq(EmpPieceView::getEmpPieceId,empPieceId)
                .one();

        Long positionId=empPieceView.getPositionId();

        CoefficientView coefficientView=CoefficientViewService.lambdaQuery()
                .eq(CoefficientView::getEmpId,empId)
                .eq(CoefficientView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        LambdaQueryWrapper<EmpPieceView> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmpPieceView::getPositionId,positionId)
                .eq(EmpPieceView::getEmpId,empId)
                .eq(EmpPieceView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpPieceView> pieceList= EmpPieceViewMapper.selectList(queryWrapper2);

        AtomicReference<BigDecimal> pieceTotal = new AtomicReference<>(new BigDecimal(0));
        if(!pieceList.isEmpty()){
            pieceList.forEach(x->{
                BigDecimal currentScore =(x.getTargetNum()
                        .multiply(new BigDecimal(x.getWorkOrder()))
                        .multiply(x.getQuality())
                        .divide(new BigDecimal(100),2));
                pieceTotal.updateAndGet(total -> total.add(currentScore));
            });
        }

        LambdaQueryWrapper<EmployeePosition> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(EmployeePosition::getEmpId,empId)
                .eq(EmployeePosition::getPositionId,positionId)
                .eq(EmployeePosition::getState,1);
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper5);

        EmpWage empWage= EmpWageService.lambdaQuery()
                .eq(EmpWage::getEmpId,empId)
                .eq(EmpWage::getPositionId,positionId)
                .eq(EmpWage::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        empWage.setPieceWage(pieceTotal.get());

        BigDecimal result = empWage.getOkrWage()
                .add(empWage.getScoreWage())
                .add(empWage.getKpiWage())
                .add(empWage.getPieceWage())
                .add(empWage.getRewardWage())
                .add(coefficientView.getWage()
                        .multiply(EmployeePosition.getPosiPercent())
                        .divide(new BigDecimal(100),2));
        empWage.setTotal(result);
        EmpWageService.updateById(empWage);
    }

    @Override
    public EmpPiece changeState(EmpPieceView empPieceView){
        EmpPiece empPiece=new EmpPiece();
        empPiece.setId(empPieceView.getEmpPieceId());
        empPiece.setEmpId(empPieceView.getEmpId());
        empPiece.setPieceId(empPieceView.getPieceId());
        empPiece.setWorkOrder(empPiece.getWorkOrder());
        empPiece.setQuality(empPiece.getQuality());
        empPiece.setState(Short.parseShort("2"));
        return empPiece;
    }
}
