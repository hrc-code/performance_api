package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.EmpKpiViewMapper;
import com.example.workflow.mapper.EmpOkrViewMapper;
import com.example.workflow.mapper.EmpPieceViewMapper;
import com.example.workflow.mapper.EmpRewardViewMapper;
import com.example.workflow.mapper.EmpScoreViewMapper;
import com.example.workflow.mapper.EmployeeCoefficientMapper;
import com.example.workflow.mapper.ResultOkrViewMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.model.entity.CoefficientView;
import com.example.workflow.model.entity.EmpCoefficient;
import com.example.workflow.model.entity.EmpKpi;
import com.example.workflow.model.entity.EmpKpiView;
import com.example.workflow.model.entity.EmpOkrView;
import com.example.workflow.model.entity.EmpPiece;
import com.example.workflow.model.entity.EmpPieceView;
import com.example.workflow.model.entity.EmpRewardView;
import com.example.workflow.model.entity.EmpScoreView;
import com.example.workflow.model.entity.EmpWage;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.service.EmpKpiService;
import com.example.workflow.service.EmpPieceService;
import com.example.workflow.service.EmpScoreService;
import com.example.workflow.service.EmployeeCoefficientService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EmployeeCoefficientImpl extends ServiceImpl<EmployeeCoefficientMapper, EmpCoefficient> implements EmployeeCoefficientService {
    @Autowired
    private com.example.workflow.service.CoefficientViewService CoefficientViewService;
    @Autowired
    private com.example.workflow.service.EmpCoefficientService EmpCoefficientService;
    @Autowired
    private com.example.workflow.mapper.ResultScoreEmpViewMapper ResultScoreEmpViewMapper;
    @Autowired
    private com.example.workflow.mapper.ResultPieceEmpViewMapper ResultPieceEmpViewMapper;
    @Autowired
    private com.example.workflow.mapper.ResultKpiEmpViewMapper ResultKpiEmpViewMapper;
    @Autowired
    private com.example.workflow.mapper.CoefficientViewMapper CoefficientViewMapper;
    @Autowired
    private com.example.workflow.service.EmpWageService EmpWageService;
    @Autowired
    private com.example.workflow.service.EmployeePositionService EmployeePositionService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private ResultOkrViewMapper ResultOkrViewMapper;
    @Autowired
    private EmpScoreViewMapper EmpScoreViewMapper;
    @Autowired
    private EmpPieceViewMapper EmpPieceViewMapper;
    @Autowired
    private EmpKpiViewMapper EmpKpiViewMapper;
    @Autowired
    private EmpOkrViewMapper EmpOkrViewMapper;
    @Autowired
    private EmpRewardViewMapper EmpRewardViewMapper;
    @Autowired
            private EmpScoreService EmpScoreService;
    @Autowired
    private EmpPieceService EmpPieceService;
    @Autowired
            private EmpKpiService EmpKpiService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @Override
    public void fileOne(Long empId,Long positionId){

        CoefficientView coefficientView=CoefficientViewService.lambdaQuery()
                .eq(CoefficientView::getEmpId,empId)
                .eq(CoefficientView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        LambdaQueryWrapper<EmpScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpScoreView::getPositionId,positionId)
                .eq(EmpScoreView::getEmpId,empId)
                .ne(EmpScoreView::getState,0)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpScoreView> scoreList= EmpScoreViewMapper.selectList(queryWrapper);
        /*scoreList.forEach(x->{
            EmpScore empScore=EmpScoreService.changeState(x);
            EmpScoreService.updateById(empScore);
        });*/

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

        LambdaQueryWrapper<EmpPieceView> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmpPieceView::getPositionId,positionId)
                .eq(EmpPieceView::getEmpId,empId)
                .ne(EmpPieceView::getState,0)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpPieceView> pieceList= EmpPieceViewMapper.selectList(queryWrapper2);
        pieceList.forEach(x->{
            EmpPiece empPiece=EmpPieceService.changeState(x);
            EmpPieceService.updateById(empPiece);
        });

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

        LambdaQueryWrapper<EmpKpiView> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(EmpKpiView::getPositionId,positionId)
                .eq(EmpKpiView::getEmpId,empId)
                .ne(EmpKpiView::getState,0)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpKpiView> kpiList= EmpKpiViewMapper.selectList(queryWrapper3);
        kpiList.forEach(x->{
            EmpKpi empKpi=EmpKpiService.changeState(x);
            EmpKpiService.updateById(empKpi);
        });

        AtomicReference<BigDecimal> kpiTotal = new AtomicReference<>(new BigDecimal(0));
        if(!kpiList.isEmpty()){
            kpiList.forEach(x->{
                if(x.getCorrectedValue()==null){
                    kpiTotal.updateAndGet(total -> total.add(x.getResult()));
                }
                else {
                    kpiTotal.updateAndGet(total -> total.add(x.getCorrectedValue()));
                }
            });
        }

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
                    BigDecimal currentScore =x.getScore()
                            .multiply(new BigDecimal(x.getKeyWeight()))
                            .divide(new BigDecimal(100),2)
                            .multiply(new BigDecimal(x.getTotalScore()))
                            .divide(new BigDecimal(100),2);
                    okrTotal.updateAndGet(total -> total.add(currentScore));
                }
                else {
                    BigDecimal currentScore =x.getCorrectedValue()
                            .multiply(new BigDecimal(x.getKeyWeight()))
                            .divide(new BigDecimal(100),2)
                            .multiply(new BigDecimal(x.getTotalScore()))
                            .divide(new BigDecimal(100),2);
                    okrTotal.updateAndGet(total -> total.add(currentScore));
                }
            });
        }

        LambdaQueryWrapper<EmpRewardView> queryWrapper8=new LambdaQueryWrapper<>();
        queryWrapper8.eq(EmpRewardView::getPositionId,positionId)
                .eq(EmpRewardView::getEmpId,empId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpRewardView> rewardViewsList= EmpRewardViewMapper.selectList(queryWrapper8);

        AtomicReference<BigDecimal> rewardTotal = new AtomicReference<>(new BigDecimal(0));
        if(!rewardViewsList.isEmpty()){
            rewardViewsList.forEach(x->{
                rewardTotal.updateAndGet(total -> total.add(x.getReward()));
            });
        }

        LambdaQueryWrapper<EmployeePosition> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(EmployeePosition::getEmpId,empId)
                .eq(EmployeePosition::getPositionId,positionId)
                .eq(EmployeePosition::getState,1);
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper5);

        EmpWage wage=new EmpWage();
        wage=EmpWageService.lambdaQuery().eq(EmpWage::getEmpId,empId)
                .eq(EmpWage::getPositionId,positionId)
                .eq(EmpWage::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        if(wage==null){
            wage=new EmpWage();

            wage.setEmpId(empId);
            wage.setPositionId(positionId);
            if(!scoreTotal.get().equals(new BigDecimal(0))){
                wage.setScoreWage(scoreTotal.get()
                        .multiply(coefficientView.getPerformanceWage())
                        .multiply(new BigDecimal(coefficientView.getRegionCoefficient()))
                        .divide(new BigDecimal(100),2)
                        .multiply(coefficientView.getPositionCoefficient())
                        .multiply(EmployeePosition.getPosiPercent())
                        .divide(new BigDecimal(100),2));
            }
            else{
                wage.setScoreWage(scoreTotal.get());
            }
            if(!okrTotal.get().equals(new BigDecimal(0))){
                wage.setOkrWage(okrTotal.get()
                        .multiply(coefficientView.getPerformanceWage())
                        .multiply(new BigDecimal(coefficientView.getRegionCoefficient()))
                        .divide(new BigDecimal(100),2)
                        .multiply(coefficientView.getPositionCoefficient())
                        .multiply(EmployeePosition.getPosiPercent())
                        .divide(new BigDecimal(100),2));
            }
            else{
                wage.setOkrWage(okrTotal.get());
            }
            wage.setKpiWage(kpiTotal.get());
            wage.setPieceWage(pieceTotal.get());
            wage.setRewardWage(rewardTotal.get());

            BigDecimal result = wage.getOkrWage()
                    .add(wage.getScoreWage())
                    .add(wage.getKpiWage())
                    .add(wage.getPieceWage())
                    .add(wage.getRewardWage())
                    .add(coefficientView.getWage()
                            .multiply(EmployeePosition.getPosiPercent())
                            .divide(new BigDecimal(100),2));
            wage.setTotal(result);

            EmpWageService.save(wage);
        }
        else{
                if(!scoreTotal.get().equals(new BigDecimal(0))){
                    wage.setScoreWage(scoreTotal.get()
                            .multiply(coefficientView.getPerformanceWage())
                            .multiply(new BigDecimal(coefficientView.getRegionCoefficient()))
                            .divide(new BigDecimal(100),2)
                            .multiply(coefficientView.getPositionCoefficient())
                            .multiply(EmployeePosition.getPosiPercent())
                            .divide(new BigDecimal(100),2));
                }
                else{
                    wage.setScoreWage(scoreTotal.get());
                }
                if(!okrTotal.get().equals(new BigDecimal(0))){
                    System.out.println(okrTotal.get());
                    wage.setOkrWage(okrTotal.get()
                            .multiply(coefficientView.getPerformanceWage())
                            .multiply(new BigDecimal(coefficientView.getRegionCoefficient()))
                            .divide(new BigDecimal(100),2)
                            .multiply(coefficientView.getPositionCoefficient())
                            .multiply(EmployeePosition.getPosiPercent())
                            .divide(new BigDecimal(100),2));
                }
                else{
                    wage.setOkrWage(okrTotal.get());
                }
                wage.setKpiWage(kpiTotal.get());
                wage.setPieceWage(pieceTotal.get());
                wage.setRewardWage(rewardTotal.get());

                BigDecimal result = wage.getOkrWage()
                        .add(wage.getScoreWage())
                        .add(wage.getKpiWage())
                        .add(wage.getPieceWage())
                        .add(wage.getRewardWage())
                        .add(coefficientView.getWage()
                                .multiply(EmployeePosition.getPosiPercent())
                                .divide(new BigDecimal(100),2));
                wage.setTotal(result);

                EmpWageService.updateById(wage);

            }

    }
}
