package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.*;
import com.example.workflow.mapper.EmpScoreMapper;
import com.example.workflow.mapper.EmpScoreViewMapper;
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
public class EmpScoreServiceImpl extends ServiceImpl<EmpScoreMapper, EmpScore> implements EmpScoreService {
    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @Autowired
    private EmpScoreViewService EmpScoreViewService;
    @Autowired
    private EmpScoreViewMapper EmpScoreViewMapper;
    @Autowired
    private EmpWageService EmpWageService;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private CoefficientViewService CoefficientViewService;

    @Override
    public void reChange(Long empId, Long empScoreId){
        EmpScoreView empScoreView= EmpScoreViewService.lambdaQuery()
                .eq(EmpScoreView::getEmpScoreId,empScoreId)
                .one();

        Long positionId=empScoreView.getPositionId();

        LambdaQueryWrapper<EmployeePosition> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(EmployeePosition::getEmpId,empId)
                .eq(EmployeePosition::getPositionId,positionId)
                .eq(EmployeePosition::getState,1);
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper5);

        LambdaQueryWrapper<EmpScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpScoreView::getPositionId,positionId)
                .eq(EmpScoreView::getEmpId,empId)
                .ne(EmpScoreView::getState,0)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpScoreView> scoreList= EmpScoreViewMapper.selectList(queryWrapper);

        AtomicReference<BigDecimal> scoreTotal = new AtomicReference<>(new BigDecimal(0));
        if(!scoreList.isEmpty()&&scoreList != null){
            scoreList.forEach(x->{
                if(x.getCorrectedValue()==null){
                    BigDecimal currentScore =(x.getScore()
                            .multiply(x.getScorePercent())
                            .divide(new BigDecimal(100),2)
                            .multiply(x.getAssessorPercent()))
                            .divide(new BigDecimal(100),2);
                    scoreTotal.updateAndGet(total -> total.add(currentScore));
                }
                else {
                    BigDecimal currentScore =(x.getCorrectedValue()
                            .multiply(x.getScorePercent())
                            .divide(new BigDecimal(100),2)
                            .multiply(x.getAssessorPercent()))
                            .divide(new BigDecimal(100),2);
                    scoreTotal.updateAndGet(total -> total.add(currentScore));
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

        CoefficientView coefficientView=CoefficientViewService.lambdaQuery()
                .eq(CoefficientView::getEmpId,empId)
                .eq(CoefficientView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        empWage.setScoreWage(scoreTotal.get()
                .multiply(coefficientView.getPerformanceWage())
                .multiply(new BigDecimal(coefficientView.getRegionCoefficient()))
                .divide(new BigDecimal(100),2)
                .multiply(coefficientView.getPositionCoefficient())
                .multiply(EmployeePosition.getPosiPercent())
                .divide(new BigDecimal(100),2));

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
    public EmpScore changeState (EmpScoreView empScoreView){
        EmpScore empScore=new EmpScore();
        empScore.setId(empScoreView.getEmpScoreId());
        empScore.setScoreAssessorsId(empScoreView.getScoreAssessorsId());
        empScore.setEmpId(empScoreView.getEmpId());
        empScore.setScore(empScoreView.getScore());
        empScore.setCorrectedValue(empScoreView.getCorrectedValue());
        empScore.setState(Short.parseShort("2"));

        return empScore;
    }
}
