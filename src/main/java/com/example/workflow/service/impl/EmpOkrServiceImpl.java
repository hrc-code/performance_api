package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.EmpOkrMapper;
import com.example.workflow.mapper.EmpOkrViewMapper;
import com.example.workflow.model.entity.EmpOkr;
import com.example.workflow.model.entity.EmpOkrView;
import com.example.workflow.model.entity.EmpWage;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.service.EmpOkrService;
import com.example.workflow.service.EmpOkrViewService;
import com.example.workflow.service.EmpWageService;
import com.example.workflow.service.EmployeePositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EmpOkrServiceImpl extends ServiceImpl<EmpOkrMapper, EmpOkr> implements EmpOkrService {

    @Autowired
    private EmpOkrViewService EmpOkrViewService;
    @Autowired
            private EmpOkrViewMapper EmpOkrViewMapper;
    @Autowired
            private EmpWageService EmpWageService;
    @Autowired
            private EmployeePositionService EmployeePositionService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @Override
    public void reChange(Long empOkrId, Long empId){
        EmpOkrView empOkrView= EmpOkrViewService.lambdaQuery()
                .eq(EmpOkrView::getEmpOkrId,empOkrId)
                .one();

        Long positionId=empOkrView.getPositionId();

        LambdaQueryWrapper<EmpOkrView> queryWrapper6=new LambdaQueryWrapper<>();
        queryWrapper6.eq(EmpOkrView::getPositionId,positionId)
                .eq(EmpOkrView::getLiaEmpId,empId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpOkrView> okrList= EmpOkrViewMapper.selectList(queryWrapper6);

        AtomicReference<BigDecimal> okrTotal = new AtomicReference<>(new BigDecimal(0));
        if(!okrList.isEmpty()){
            okrList.forEach(x->{
                if(x.getCorrectedValue()==null){
                    okrTotal.updateAndGet(total -> total.add(x.getScore()));
                }
                else {
                    okrTotal.updateAndGet(total -> total.add(x.getCorrectedValue()));
                }
            });
        }

        EmpWage empWage= EmpWageService.lambdaQuery()
                .eq(EmpWage::getEmpId,empId)
                .eq(EmpWage::getPositionId,positionId)
                .eq(EmpWage::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        empWage.setOkrWage(okrTotal.get());

        LambdaQueryWrapper<EmployeePosition> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(EmployeePosition::getEmpId,empId)
                .eq(EmployeePosition::getPositionId,positionId)
                .eq(EmployeePosition::getState,1);
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper5);

        BigDecimal result = empWage.getOkrWage()
                .add(empWage.getKpiWage())
                .add(empWage.getPieceWage())
                .add(empWage.getRewardWage())
                .add(empWage.getScoreWage())
                .multiply(EmployeePosition.getPosiPercent());
        empWage.setTotal(result);
        EmpWageService.updateById(empWage);
    }

}
