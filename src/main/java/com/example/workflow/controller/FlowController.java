package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.workflow.common.R;
import com.example.workflow.entity.ActReDeployment;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.TaskView;
import com.example.workflow.mapper.ActReDeploymentMapper;
import com.example.workflow.mapper.EmployeePositionMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.PositionService;
import com.example.workflow.service.ResultSecondExamineService;
import com.example.workflow.service.ResultThirdExamineService;
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
        LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId,obj.getString("positionId"));
        List<EmployeePosition> empList=EmployeePositionMapper.selectList(queryWrapper);

        for(EmployeePosition x:empList){
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
            TaskView task=TaskViewMapper.selectOne(Wrapper);

            if(task!=null){
                runtimeService.suspendProcessInstanceById(task.getProcInstId());
            }
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

        empList.forEach(x->{
            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,x.getProcessDefinitionId());
            List<TaskView> taskList=TaskViewMapper.selectList(Wrapper);

            taskList.forEach(y->{
                runtimeService.deleteProcessInstance(y.getProcInstId(),"删除原因");
            });
        });

        UpdateWrapper<Position> updateWrapper =new UpdateWrapper<>();
        updateWrapper.set("audit_status", '0').eq("id",obj.getString("positionId"));
        PositionService.update(updateWrapper);
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


