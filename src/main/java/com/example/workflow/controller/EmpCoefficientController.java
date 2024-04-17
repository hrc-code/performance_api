package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.entity.CoefficientView;
import com.example.workflow.entity.EmpCoefficient;
import com.example.workflow.entity.EmpKpiView;
import com.example.workflow.entity.EmpOkrView;
import com.example.workflow.entity.EmpPieceView;
import com.example.workflow.entity.EmpRewardView;
import com.example.workflow.entity.EmpScoreView;
import com.example.workflow.entity.EmpWage;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.TaskView;
import com.example.workflow.mapper.CoefficientViewMapper;
import com.example.workflow.mapper.EmpKpiViewMapper;
import com.example.workflow.mapper.EmpOkrViewMapper;
import com.example.workflow.mapper.EmpPieceViewMapper;
import com.example.workflow.mapper.EmpRewardViewMapper;
import com.example.workflow.mapper.EmpScoreViewMapper;
import com.example.workflow.mapper.ResultKpiEmpViewMapper;
import com.example.workflow.mapper.ResultOkrViewMapper;
import com.example.workflow.mapper.ResultPieceEmpViewMapper;
import com.example.workflow.mapper.ResultScoreEmpViewMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.service.CoefficientViewService;
import com.example.workflow.service.EmpCoefficientService;
import com.example.workflow.service.EmpWageService;
import com.example.workflow.service.EmployeePositionService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequestMapping("/EmpCoefficient")
public class EmpCoefficientController {
    @Autowired
    private CoefficientViewService CoefficientViewService;
    @Autowired
    private EmpCoefficientService EmpCoefficientService;
    @Autowired
    private ResultScoreEmpViewMapper ResultScoreEmpViewMapper;
    @Autowired
    private ResultPieceEmpViewMapper ResultPieceEmpViewMapper;
    @Autowired
    private ResultKpiEmpViewMapper ResultKpiEmpViewMapper;
    @Autowired
    private CoefficientViewMapper CoefficientViewMapper;
    @Autowired
    private EmpWageService EmpWageService;
    @Autowired
    private EmployeePositionService EmployeePositionService;
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
    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @GetMapping("/page")
    private R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){

        Page<CoefficientView> pageInfo=new Page<CoefficientView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<CoefficientView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .orderByDesc(CoefficientView::getEmpId);
        CoefficientViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/copy")
    private R copy(){
        List<EmpCoefficient> list= EmpCoefficientService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(!list.isEmpty())
            return R.error("本月员工系数已复制，请勿重复操作");

        EmpCoefficientService.monthCopy();

        return R.success();
    }

    @GetMapping("/coefficientPastPage")
    public R<Page> coefficientPastPage(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<CoefficientView> pageInfo=new Page<CoefficientView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<CoefficientView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        CoefficientViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @PostMapping("/update")
    private R<EmpCoefficient> update(@RequestBody EmpCoefficient one){
        EmpCoefficientService.updateById(one);

        return R.success();
    }

    @PostMapping("/file")
    private R file(@RequestBody JSONObject obj){
        Long empId=Long.valueOf(obj.getString("empId"));
        Long positionId=Long.valueOf(obj.getString("positionId"));

        LambdaQueryWrapper<CoefficientView> queryWrapper4=new LambdaQueryWrapper<>();
        queryWrapper4.eq(CoefficientView::getEmpId,empId)
                .eq(CoefficientView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        CoefficientView coefficientView= CoefficientViewMapper.selectOne(queryWrapper4);

        LambdaQueryWrapper<EmpScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpScoreView::getPositionId,positionId)
                .eq(EmpScoreView::getEmpId,empId)
                .eq(EmpScoreView::getState,1)
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
                pieceTotal.updateAndGet(total -> total.add(x.getTargetNum()));
            });
        }

        LambdaQueryWrapper<EmpKpiView> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(EmpKpiView::getPositionId,positionId)
                .eq(EmpKpiView::getEmpId,empId)
                .eq(EmpKpiView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpKpiView> kpiList= EmpKpiViewMapper.selectList(queryWrapper3);

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
                    okrTotal.updateAndGet(total -> total.add(x.getScore()));
                }
                else {
                    okrTotal.updateAndGet(total -> total.add(x.getCorrectedValue()));
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

        EmpWage wage=new EmpWage();
        wage.setEmpId(empId);
        wage.setPositionId(positionId);
        if(!scoreTotal.get().equals(new BigDecimal(0))){
            wage.setScoreWage(scoreTotal.get().multiply(coefficientView.getPerformanceWage())
                    .divide(new BigDecimal(100),2)
                    .multiply(new BigDecimal(coefficientView.getRegionCoefficient()))
                    .multiply(coefficientView.getPositionCoefficient()));
        }
        else{
            wage.setScoreWage(scoreTotal.get());
        }
        if(!okrTotal.equals(new BigDecimal(0))){
            wage.setOkrWage(okrTotal.get().multiply(coefficientView.getPerformanceWage())
                    .multiply(new BigDecimal(coefficientView.getRegionCoefficient()))
                    .divide(new BigDecimal(100),2)
                    .multiply(coefficientView.getPositionCoefficient()));;
        }
        else{
            wage.setOkrWage(okrTotal.get());
        }
        wage.setKpiWage(kpiTotal.get());
        wage.setPieceWage(pieceTotal.get());
        wage.setRewardWage(rewardTotal.get());

        LambdaQueryWrapper<EmployeePosition> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(EmployeePosition::getEmpId,empId)
                .eq(EmployeePosition::getPositionId,positionId)
                .eq(EmployeePosition::getState,1);
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper5);

        BigDecimal result = okrTotal.get()
                .add(kpiTotal.get())
                .add(scoreTotal.get())
                .add(pieceTotal.get())
                .add(rewardTotal.get())
                .multiply(EmployeePosition.getPosiPercent());
        wage.setTotal(result);

        EmpWageService.save(wage);

        LambdaQueryWrapper<TaskView> queryWrapper7=new LambdaQueryWrapper<>();
        queryWrapper7.eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId())
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getAssignee,obj.getString("assessorId"));
        TaskView task= TaskViewMapper.selectOne(queryWrapper7);
        taskService.complete(task.getId());

        return R.success();
    }
}
