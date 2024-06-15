package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.ActReDeployment;
import com.example.workflow.entity.BackWait;
import com.example.workflow.entity.EmpKpi;
import com.example.workflow.entity.EmpOkr;
import com.example.workflow.entity.EmpPiece;
import com.example.workflow.entity.EmpPositionView;
import com.example.workflow.entity.EmpScore;
import com.example.workflow.entity.EmpWage;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.OkrKey;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.PositionAssessor;
import com.example.workflow.entity.PositionKpi;
import com.example.workflow.entity.PositionPiece;
import com.example.workflow.entity.PositionScore;
import com.example.workflow.entity.ResultFourthExamine;
import com.example.workflow.entity.ResultSecondExamine;
import com.example.workflow.entity.ResultThirdExamine;
import com.example.workflow.entity.ScoreAssessors;
import com.example.workflow.entity.TaskView;
import com.example.workflow.mapper.ActReDeploymentMapper;
import com.example.workflow.mapper.EmployeePositionMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.service.BackWaitService;
import com.example.workflow.service.EmpOkrService;
import com.example.workflow.service.EmpPositionViewService;
import com.example.workflow.service.EmpScoreService;
import com.example.workflow.service.EmpWageService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.OkrKeyService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.PositionKpiSerivce;
import com.example.workflow.service.PositionPieceService;
import com.example.workflow.service.PositionScoreService;
import com.example.workflow.service.PositionService;
import com.example.workflow.service.ResultFourthExamineService;
import com.example.workflow.service.ResultSecondExamineService;
import com.example.workflow.service.ResultThirdExamineService;
import com.example.workflow.service.ScoreAssessorsService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/flow")
public class FlowController {

    @Autowired
    private ActReDeploymentMapper ActReDeploymentMapper;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private EmployeePositionMapper EmployeePositionMapper;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private PositionService PositionService;
    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private ResultThirdExamineService ResultThirdExamineService;
    @Autowired
    private ResultSecondExamineService ResultSecondExamineService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
            private EmpPositionViewService EmpPositionViewService;
    @Autowired
            private PositionScoreService PositionScoreService;
    @Autowired
            private ScoreAssessorsService ScoreAssessorsService;
    @Autowired
            private PositionPieceService PositionPieceService;
    @Autowired
            private PositionKpiSerivce PositionKpiSerivce;
    @Autowired
            private EmpScoreService EmpScoreService;
    @Autowired
            private OkrKeyService OkrKeyService;
    @Autowired
            private EmpOkrService EmpOkrService;
    @Autowired
            private ResultFourthExamineService ResultFourthExamineService;
    @Autowired
            private EmpWageService EmpWageService;
    @Autowired
            private BackWaitService BackWaitService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @PostMapping("/list")
    private R<List<ActReDeployment>> list(){
        List<ActReDeployment> list= ActReDeploymentMapper.selectList(null);

        return R.success(list);
    }

    @PostMapping("/deployee")
    private void deploy(String name,String resource){
        Deployment deploy = repositoryService.createDeployment()
                .name(name)
                .addClasspathResource(resource)
                .deploy();
        System.out.println("deploy.getId() = " + deploy.getId());
    }


    @PostMapping("/startFlow")
    private R startFlow(@RequestBody JSONObject obj){
        PositionAssessor assessor=PositionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpPositionView::getPositionId,obj.getString("positionId"));
        List<EmpPositionView> empList= EmpPositionViewService.list(queryWrapper);
        if(empList.isEmpty()){
            return R.error("没有员工在该岗位");
        }

        if(empList.get(0).getType()==5){
            if(assessor.getSecondAssessorId()==null||assessor.getFourthAssessorId()==null||assessor.getThirdAssessorId()==null){
                return R.error("该岗位未配置对应的审核人与审核时限，请前往配置");
            }
        }
        else if(empList.get(0).getType()==4){
            if(assessor.getSecondAssessorId()==null||assessor.getThirdAssessorId()==null){
                return R.error("该岗位未配置对应的审核人与审核时限，请前往配置");
            }
        }
        else if(empList.get(0).getType()==3){
            if(assessor.getSecondAssessorId()==null){
                return R.error("该岗位未配置对应的审核人与审核时限，请前往配置");
            }
        }

        for(EmpPositionView x:empList){
            Map<String,Object> map = new HashMap<>();
            map.put("declarer",obj.getString("empId"));

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));

            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey(x.getProcessKey(),map);

            UpdateWrapper<EmployeePosition> updateWrapper =new UpdateWrapper<>();
            updateWrapper
                    .set("process_definition_id",processInstance.getId())
                    .eq("emp_id",x.getEmpId())
                    .eq("position_id",obj.getString("positionId"))
                    .eq("state",1);
            EmployeePositionService.update(updateWrapper);
        }
        UpdateWrapper<Position> updateWrapper =new UpdateWrapper<>();
        updateWrapper
                .set("audit_status", '1')
                .eq("id",obj.getString("positionId"))
                .eq("state",1);;
        PositionService.update(updateWrapper);

        return R.success();
    }


    @PostMapping("/stopFlow")
    private R stopFlow(@RequestBody JSONObject obj){
        LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId,obj.getString("positionId"));
        List<EmployeePosition> empList=EmployeePositionMapper.selectList(queryWrapper);

        empList.forEach(x->{
            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,x.getProcessDefinitionId())
                    .eq(TaskView::getState,"ACTIVE");
            List<TaskView> taskViewList=TaskViewMapper.selectList(Wrapper);

            taskViewList.forEach(y->{
                if(y!=null){
                    runtimeService.suspendProcessInstanceById(y.getProcInstId());
                }
            });

        });

        UpdateWrapper<Position> updateWrapper =new UpdateWrapper<>();
        updateWrapper.set("audit_status", '2').eq("id",obj.getString("positionId"));
        PositionService.update(updateWrapper);

        return R.success();
    }


    @PostMapping("/restoreFlow")
    private R restoreFlow(@RequestBody JSONObject obj){
        LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId,obj.getString("positionId"));
        List<EmployeePosition> empList=EmployeePositionMapper.selectList(queryWrapper);

        empList.forEach(x->{
            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,x.getProcessDefinitionId());
            TaskView task=TaskViewMapper.selectOne(Wrapper);
            if(task!=null) {
                runtimeService.activateProcessInstanceById(task.getProcInstId());
            }
        });

        UpdateWrapper<Position> updateWrapper =new UpdateWrapper<>();
        updateWrapper.set("audit_status", '1').eq("id",obj.getString("positionId"));
        PositionService.update(updateWrapper);

        return R.success();
    }


    @PostMapping("/deleteFlow")
    private R deleteFlow(@RequestBody JSONObject obj){
        LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId,obj.getString("positionId"));
        List<EmployeePosition> empList=EmployeePositionMapper.selectList(queryWrapper);

        List<PositionScore> positionScores=PositionScoreService.lambdaQuery()
                .eq(PositionScore::getPositionId,obj.getString("positionId"))
                .eq(PositionScore::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .list();

        List<ScoreAssessors> delete1=new ArrayList<>();
        positionScores.forEach(x->{
            List<ScoreAssessors> scoreAssessorss=ScoreAssessorsService.lambdaQuery()
                    .eq(ScoreAssessors::getPositionScoreId,x.getId())
                    .eq(ScoreAssessors::getState,1)
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();
            scoreAssessorss.forEach(y->{
                delete1.add(y);
            });
        });

        List<PositionPiece> delete2=PositionPieceService.lambdaQuery()
                .eq(PositionPiece::getPositionId,obj.getString("positionId"))
                .eq(PositionPiece::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        List<PositionKpi> delete3=PositionKpiSerivce.lambdaQuery()
                .eq(PositionKpi::getPositionId,obj.getString("positionId"))
                .eq(PositionKpi::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        empList.forEach(x->{
            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,x.getProcessDefinitionId());
            List<TaskView> taskList=TaskViewMapper.selectList(Wrapper);

            taskList.forEach(y->{
                runtimeService.deleteProcessInstance(y.getProcInstId(),"删除原因");
            });

            if(!delete1.isEmpty()){
                delete1.forEach(y->{
                    LambdaQueryWrapper<EmpScore> queryWrapper1=new LambdaQueryWrapper<>();
                    queryWrapper1.eq(EmpScore::getScoreAssessorsId,y.getId())
                            .eq(EmpScore::getEmpId,x.getEmpId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                    EmpScoreService.remove(queryWrapper1);
                });
            }
            if(!delete2.isEmpty()){
                delete2.forEach(y->{
                    LambdaQueryWrapper<EmpPiece> queryWrapper1=new LambdaQueryWrapper<>();
                    queryWrapper1.eq(EmpPiece::getPieceId,y.getPieceId())
                            .eq(EmpPiece::getEmpId,x.getEmpId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                });
            }
            if(!delete3.isEmpty()){
                delete3.forEach(y->{
                    LambdaQueryWrapper<EmpKpi> queryWrapper1=new LambdaQueryWrapper<>();
                    queryWrapper1.eq(EmpKpi::getKpiId,y.getKpiId())
                            .eq(EmpKpi::getEmpId,x.getEmpId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                });
            }

            List<OkrKey> delete4=OkrKeyService.lambdaQuery()
                    .eq(OkrKey::getPositionId,obj.getString("positionId"))
                    .eq(OkrKey::getLiaEmpId,x.getEmpId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();

            if(!delete4.isEmpty()){
                delete4.forEach(y->{
                    LambdaQueryWrapper<EmpOkr> queryWrapper1=new LambdaQueryWrapper<>();
                    queryWrapper1.eq(EmpOkr::getEmpId,x.getEmpId())
                            .eq(EmpOkr::getOkrKeyId,y.getId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                    EmpOkrService.remove(queryWrapper1);
                });
            }

            LambdaQueryWrapper<EmpWage> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(EmpWage::getEmpId,x.getEmpId())
                    .eq(EmpWage::getPositionId,obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
            EmpWageService.remove(queryWrapper1);
        });

        LambdaQueryWrapper<ResultFourthExamine> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(ResultFourthExamine::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ResultFourthExamineService.remove(queryWrapper1);

        LambdaQueryWrapper<ResultThirdExamine> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(ResultThirdExamine::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ResultThirdExamineService.remove(queryWrapper2);

        LambdaQueryWrapper<ResultSecondExamine> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(ResultSecondExamine::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ResultSecondExamineService.remove(queryWrapper3);

        UpdateWrapper<Position> updateWrapper =new UpdateWrapper<>();
        updateWrapper.set("audit_status", '0').eq("id",obj.getString("positionId"));
        PositionService.update(updateWrapper);

        List<BackWait> backWaitList=BackWaitService.lambdaQuery()
                .eq(BackWait::getPositionId,obj.getString("positionId"))
                .list();
        backWaitList.forEach(x->{
            runtimeService.deleteProcessInstance(x.getProcessDefineId(),"删除原因");
            BackWaitService.removeById(x);
        });

        return R.success();
    }

    @PostMapping("/updateFlowOne")
    public R updateFlowOne(@RequestBody JSONObject obj){
        /*Long empId=Long.valueOf(obj.getString("empId"));

        LambdaQueryWrapper<EmployeePosition> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(EmployeePosition::getEmpId,String.valueOf(empId));
        Long positionId=EmployeePositionMapper.selectOne(Wrapper).getPositionId();

        LambdaQueryWrapper<TaskView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getStartUserId,String.valueOf(empId));
        TaskView task=TaskViewMapper.selectOne(queryWrapper);

        //评分
        List<String> assessorList1 = new ArrayList<>();
        LambdaQueryWrapper<ScoreRule> query1=new LambdaQueryWrapper<>();
        query1.eq(ScoreRule::getPositionId,positionId);
        List<ScoreRule> num1= ScoreRuleService.list(query1);
        num1.forEach(x->{
            LambdaQueryWrapper<ScoreAssessors> query=new LambdaQueryWrapper<>();
            query.eq(ScoreAssessors::getRuleId,x.getId());
            List<ScoreAssessors> assessor= ScoreAssessorsService.list(query);
            assessor.forEach(y->{
                assessorList1.add(String.valueOf(y.getAssessorId()));
            });
        });
        List<String> assessors1=assessorList1.stream().distinct().collect(Collectors.toList());

        //计件
        List<String> assessorList2 = new ArrayList<>();
        LambdaQueryWrapper<PieceRule> query2=new LambdaQueryWrapper<>();
        query2.eq(PieceRule::getPositionId,positionId);
        List<PieceRule> num2= PieceRuleService.list(query2);
        num2.forEach(x->{
            assessorList2.add(String.valueOf(x.getAssessorId()));
        });
        List<String> assessors2=assessorList2.stream().distinct().collect(Collectors.toList());

        //KPI
        List<String> assessorList3 = new ArrayList<>();
        LambdaQueryWrapper<KpiRule> query3=new LambdaQueryWrapper<>();
        query3.eq(KpiRule::getPositionId,positionId);
        List<KpiRule> num3= KpiRuleService.list(query3);
        num3.forEach(x->{
            assessorList3.add(String.valueOf(x.getAssessorId()));
        });
        List<String> assessors3=assessorList3.stream().distinct().collect(Collectors.toList());

        Map<String,Object> map = new HashMap<>();
        if(assessors1.isEmpty()==true)
            map.put("scoreAppoint", "true");
        else
            map.put("ASList", assessors1);

        if(assessors2.isEmpty()==true)
            map.put("pieceAppoint", "true");
        else
            map.put("APList", assessors2);

        if(assessors3.isEmpty()==true)
            map.put("kpiAppoint", "true");
        else
            map.put("AKList", assessors3);
        map.put("kpiAppoint", "true");
        taskService.complete(task.getId(),map);*/

        return R.success();
    }

    @PostMapping("/updateFlowAll")
    public R updateFlowAll(@RequestBody JSONObject obj){
        /*List list=obj.getJSONArray("empList");

        list.forEach(x->{

        });

        LambdaQueryWrapper<EmployeePosition> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(EmployeePosition::getEmpId,String.valueOf(empId));
        Long positionId=EmployeePositionMapper.selectOne(Wrapper).getPositionId();

        LambdaQueryWrapper<TaskView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getStartUserId,String.valueOf(empId));
        TaskView task=TaskViewMapper.selectOne(queryWrapper);

        //评分
        List<String> assessorList1 = new ArrayList<>();
        LambdaQueryWrapper<ScoreRule> query1=new LambdaQueryWrapper<>();
        query1.eq(ScoreRule::getPositionId,positionId);
        List<ScoreRule> num1= ScoreRuleService.list(query1);
        num1.forEach(x->{
            LambdaQueryWrapper<ScoreAssessors> query=new LambdaQueryWrapper<>();
            query.eq(ScoreAssessors::getRuleId,x.getId());
            List<ScoreAssessors> assessor= ScoreAssessorsService.list(query);
            assessor.forEach(y->{
                assessorList1.add(String.valueOf(y.getAssessorId()));
            });
        });
        List<String> assessors1=assessorList1.stream().distinct().collect(Collectors.toList());

        //计件
        List<String> assessorList2 = new ArrayList<>();
        LambdaQueryWrapper<PieceRule> query2=new LambdaQueryWrapper<>();
        query2.eq(PieceRule::getPositionId,positionId);
        List<PieceRule> num2= PieceRuleService.list(query2);
        num2.forEach(x->{
            assessorList2.add(String.valueOf(x.getAssessorId()));
        });
        List<String> assessors2=assessorList2.stream().distinct().collect(Collectors.toList());

        //KPI
        List<String> assessorList3 = new ArrayList<>();
        LambdaQueryWrapper<KpiRule> query3=new LambdaQueryWrapper<>();
        query3.eq(KpiRule::getPositionId,positionId);
        List<KpiRule> num3= KpiRuleService.list(query3);
        num3.forEach(x->{
            assessorList3.add(String.valueOf(x.getAssessorId()));
        });
        List<String> assessors3=assessorList3.stream().distinct().collect(Collectors.toList());

        Map<String,Object> map = new HashMap<>();
        if(assessors1.isEmpty()==true)
            map.put("scoreAppoint", "true");
        else
            map.put("ASList", assessors1);

        if(assessors2.isEmpty()==true)
            map.put("pieceAppoint", "true");
        else
            map.put("APList", assessors2);

        if(assessors3.isEmpty()==true)
            map.put("kpiAppoint", "true");
        else
            map.put("AKList", assessors3);
        map.put("kpiAppoint", "true");
        taskService.complete(task.getId(),map);*/

        return R.success();
    }

    @PostMapping("/userId")
    private R<List<IdentityLink>> searchId(String taskId){
        List<IdentityLink> id=taskService.getIdentityLinksForTask(taskId);
        return R.success(id);
    }

    @PostMapping("/complete")
    private R complete(String id){
        List<String> assessorList1 = new ArrayList<>(4);
        assessorList1.add("userOne");
        assessorList1.add("userTwo");
        List<String> assessorList2 = new ArrayList<>(4);
        assessorList2.add("userThree");
        Map<String,Object> map = new HashMap<>();
        map.put("appoint","no");
        map.put("assessor","xxy");
        map.put("declear","q");
        map.put("pieceAppoint", "true");
        map.put("scoreAppoint", "false");
        map.put("kpiAppoint", "true");
        map.put("okrAppoint", "false");
        map.put("ASList", assessorList1);
        map.put("AKList", assessorList1);
        map.put("APList", assessorList2);
        map.put("AOList", assessorList2);
        map.put("AThirdList",assessorList2);
        taskService.complete(id,map);
        return R.success();
    }

    @PostMapping("/getComplete")
    private R<List<HistoricTaskInstance>> getCompleteList(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        HistoryService historyService = processEngine.getHistoryService();

        List<HistoricTaskInstance> completedTasks = historyService.createHistoricTaskInstanceQuery()
                .finished()
                .list();

        for (HistoricTaskInstance task : completedTasks) {
            System.out.println("Completed task id: " + task.getId());
        }

        return R.success(completedTasks);
    }

    public void SetState(EmployeePosition one){
        //评分
        /*LambdaQueryWrapper<ScoreRule> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreRule::getPositionId,one.getPositionId());
        List<ScoreRule> ScoreRuleList=ScoreRuleMapper.selectList(queryWrapper);
        if(ScoreRuleList!=null){
            one.setScoreState((short)1);
        }
        else{
            one.setScoreState((short)0);
        }

        //计件
        LambdaQueryWrapper<PieceRule> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(PieceRule::getPositionId,one.getPositionId());
        List<PieceRule> PieceRuleList=PieceRuleMapper.selectList(Wrapper);
        if(PieceRuleList!=null){
            one.setPieceState((short)1);
        }
        else{
            one.setPieceState((short)0);
        }

        //kpi
        LambdaQueryWrapper<KpiRule> query=new LambdaQueryWrapper<>();
        query.eq(KpiRule::getPositionId,one.getPositionId());
        List<KpiRule> KpiRuleList=KpiRuleMapper.selectList(query);
        if(KpiRuleList!=null){
            one.setKpiState((short)1);
        }
        else{
            one.setKpiState((short)0);
        }
        EmployeePositionService.updateById(one);*/
    }
}


