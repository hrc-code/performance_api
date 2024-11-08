package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.mapper.ActReDeploymentMapper;
import com.example.workflow.mapper.EmployeePositionMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.model.entity.ActReDeployment;
import com.example.workflow.model.entity.BackWait;
import com.example.workflow.model.entity.EmpKpi;
import com.example.workflow.model.entity.EmpOkr;
import com.example.workflow.model.entity.EmpPiece;
import com.example.workflow.model.entity.EmpPositionView;
import com.example.workflow.model.entity.EmpScore;
import com.example.workflow.model.entity.EmpWage;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.OkrKey;
import com.example.workflow.model.entity.Position;
import com.example.workflow.model.entity.PositionAssessor;
import com.example.workflow.model.entity.PositionKpi;
import com.example.workflow.model.entity.PositionPiece;
import com.example.workflow.model.entity.PositionScore;
import com.example.workflow.model.entity.ResultFourthExamine;
import com.example.workflow.model.entity.ResultSecondExamine;
import com.example.workflow.model.entity.ResultThirdExamine;
import com.example.workflow.model.entity.ScoreAssessors;
import com.example.workflow.model.entity.TaskView;
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
import com.example.workflow.utils.DateTimeUtils;
import lombok.AllArgsConstructor;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/flow")
@AllArgsConstructor
public class FlowController {

    private final ActReDeploymentMapper actReDeploymentMapper;
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final IdentityService identityService;
    private final EmployeePositionMapper employeePositionMapper;
    private final EmployeePositionService employeePositionService;
    private final PositionService positionService;
    private final PositionAssessorService positionAssessorService;
    private final ResultThirdExamineService resultThirdExamineService;
    private final ResultSecondExamineService resultSecondExamineService;
    private final TaskViewMapper taskViewMapper;
    private final EmpPositionViewService empPositionViewService;
    private final PositionScoreService positionScoreService;
    private final ScoreAssessorsService scoreAssessorsService;
    private final PositionPieceService positionPieceService;
    private final PositionKpiSerivce positionKpiSerivce;
    private final EmpScoreService empScoreService;
    private final OkrKeyService okrKeyService;
    private final EmpOkrService empOkrService;
    private final ResultFourthExamineService resultFourthExamineService;
    private final EmpWageService empWageService;
    private final BackWaitService backWaitService;



    @PostMapping("/list")
    private R<List<ActReDeployment>> list() {
        List<ActReDeployment> list = actReDeploymentMapper.selectList(null);

        return R.success(list);
    }

    @PostMapping("/deployee")
    private void deploy(String name, String resource) {
        Deployment deploy = repositoryService.createDeployment()
                .name(name)
                .addClasspathResource(resource)
                .deploy();
        System.out.println("deploy.getId() = " + deploy.getId());
    }


    @PostMapping("/startFlow")
    private R<Void> startFlow(@RequestBody JSONObject obj) {
        LocalDateTime[] time = DateTimeUtils.getTheStartAndEndTimeOfMonth();
        LocalDateTime beginTime = time[0];
        LocalDateTime endTime = time[1];
        PositionAssessor assessor = positionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId, obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        LambdaQueryWrapper<EmpPositionView> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpPositionView::getPositionId, obj.getString("positionId"));
        List<EmpPositionView> empList = empPositionViewService.list(queryWrapper);
        if (empList.isEmpty()) {
            return R.error("没有员工在该岗位");
        }

        if(empList.get(0).getType()==5){
            if(assessor.getSecondAssessorId()==null||assessor.getFourthAssessorId()==null||assessor.getThirdAssessorId()==null){
                return R.error("该岗位未配置对应的审核人与审核时限，请前往配置");
            }
        } else if (empList.get(0).getType() == 4) {
            if (assessor.getSecondAssessorId() == null || assessor.getThirdAssessorId() == null) {
                return R.error("该岗位未配置对应的审核人与审核时限，请前往配置");
            }
        } else if (empList.get(0).getType() == 3) {
            if (assessor.getSecondAssessorId() == null) {
                return R.error("该岗位未配置对应的审核人与审核时限，请前往配置");
            }
        }

        for(EmpPositionView x:empList){
            Map<String,Object> map = new HashMap<>();
            map.put("declarer",obj.getString("empId"));

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));

            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey(x.getProcessKey(), map);

            UpdateWrapper<EmployeePosition> updateWrapper =new UpdateWrapper<>();
            updateWrapper
                    .set("process_definition_id", processInstance.getId())
                    .eq("emp_id", x.getEmpId())
                    .eq("position_id", obj.getString("positionId"))
                    .eq("state", 1);
            employeePositionService.update(updateWrapper);
        }
        UpdateWrapper<Position> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .set("audit_status", '1')
                .eq("id", obj.getString("positionId"))
                .eq("state", 1);
        positionService.update(updateWrapper);

        return R.success();
    }


    @PostMapping("/stopFlow")
    private R<Void> stopFlow(@RequestBody JSONObject obj) {
        LambdaQueryWrapper<EmployeePosition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId, obj.getString("positionId"));
        List<EmployeePosition> empList = employeePositionMapper.selectList(queryWrapper);

        empList.forEach(x->{
            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,x.getProcessDefinitionId())
                    .eq(TaskView::getState,"ACTIVE");
            List<TaskView> taskViewList=TaskViewMapper.selectList(Wrapper);

            taskViewList.forEach(y -> {
                if (y != null) {
                    runtimeService.suspendProcessInstanceById(y.getProcInstId());
                }
            });

        });

        UpdateWrapper<Position> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("audit_status", '2').eq("id", obj.getString("positionId"));
        positionService.update(updateWrapper);

        return R.success();
    }


    @PostMapping("/restoreFlow")
    private R<Void> restoreFlow(@RequestBody JSONObject obj) {
        LambdaQueryWrapper<EmployeePosition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId, obj.getString("positionId"));
        List<EmployeePosition> empList = employeePositionMapper.selectList(queryWrapper);

        empList.forEach(x -> {
            LambdaQueryWrapper<TaskView> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskView::getStartUserId, x.getEmpId())
                    .eq(TaskView::getProcInstId, x.getProcessDefinitionId());
            TaskView task = taskViewMapper.selectOne(wrapper);
            if (task != null) {
                runtimeService.activateProcessInstanceById(task.getProcInstId());
            }
        });

        UpdateWrapper<Position> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("audit_status", '1').eq("id", obj.getString("positionId"));
        positionService.update(updateWrapper);

        return R.success();
    }


    @PostMapping("/deleteFlow")
    private R<Void> deleteFlow(@RequestBody JSONObject obj) {
        LocalDateTime[] time = DateTimeUtils.getTheStartAndEndTimeOfMonth();
        LocalDateTime beginTime = time[0];
        LocalDateTime endTime = time[1];
        LambdaQueryWrapper<EmployeePosition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId, obj.getString("positionId"));
        List<EmployeePosition> empList = employeePositionMapper.selectList(queryWrapper);

        List<PositionScore> positionScores = positionScoreService.lambdaQuery()
                .eq(PositionScore::getPositionId, obj.getString("positionId"))
                .eq(PositionScore::getState, 1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        List<ScoreAssessors> delete1 = new ArrayList<>();
        positionScores.forEach(x -> {
            List<ScoreAssessors> scoreAssessorss = scoreAssessorsService.lambdaQuery()
                    .eq(ScoreAssessors::getPositionScoreId, x.getId())
                    .eq(ScoreAssessors::getState, 1)
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();
            delete1.addAll(scoreAssessorss);
        });

        List<PositionPiece> delete2 = positionPieceService.lambdaQuery()
                .eq(PositionPiece::getPositionId, obj.getString("positionId"))
                .eq(PositionPiece::getState, 1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        List<PositionKpi> delete3 = positionKpiSerivce.lambdaQuery()
                .eq(PositionKpi::getPositionId, obj.getString("positionId"))
                .eq(PositionKpi::getState, 1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        empList.forEach(x -> {
            LambdaQueryWrapper<TaskView> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskView::getStartUserId, x.getEmpId())
                    .eq(TaskView::getProcInstId, x.getProcessDefinitionId());
            List<TaskView> taskList = taskViewMapper.selectList(wrapper);

            taskList.forEach(y -> runtimeService.deleteProcessInstance(y.getProcInstId(), "删除原因"));

            if (!delete1.isEmpty()) {
                delete1.forEach(y -> {
                    LambdaQueryWrapper<EmpScore> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(EmpScore::getScoreAssessorsId, y.getId())
                            .eq(EmpScore::getEmpId, x.getEmpId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                    empScoreService.remove(queryWrapper1);
                });
            }
            if (!delete2.isEmpty()) {
                delete2.forEach(y -> {
                    LambdaQueryWrapper<EmpPiece> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(EmpPiece::getPieceId, y.getPieceId())
                            .eq(EmpPiece::getEmpId, x.getEmpId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                });
            }
            if (!delete3.isEmpty()) {
                delete3.forEach(y -> {
                    LambdaQueryWrapper<EmpKpi> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(EmpKpi::getKpiId, y.getKpiId())
                            .eq(EmpKpi::getEmpId, x.getEmpId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                });
            }

            List<OkrKey> delete4 = okrKeyService.lambdaQuery()
                    .eq(OkrKey::getPositionId, obj.getString("positionId"))
                    .eq(OkrKey::getLiaEmpId, x.getEmpId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();

            if (!delete4.isEmpty()) {
                delete4.forEach(y -> {
                    LambdaQueryWrapper<EmpOkr> queryWrapper1 = new LambdaQueryWrapper<>();
                    queryWrapper1.eq(EmpOkr::getEmpId, x.getEmpId())
                            .eq(EmpOkr::getOkrKeyId, y.getId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                    empOkrService.remove(queryWrapper1);
                });
            }

            LambdaQueryWrapper<EmpWage> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(EmpWage::getEmpId, x.getEmpId())
                    .eq(EmpWage::getPositionId, obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
            empWageService.remove(queryWrapper1);
        });

        LambdaQueryWrapper<ResultFourthExamine> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ResultFourthExamine::getPositionId, obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        resultFourthExamineService.remove(queryWrapper1);

        LambdaQueryWrapper<ResultThirdExamine> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(ResultThirdExamine::getPositionId, obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        resultThirdExamineService.remove(queryWrapper2);

        LambdaQueryWrapper<ResultSecondExamine> queryWrapper3 = new LambdaQueryWrapper<>();
        queryWrapper3.eq(ResultSecondExamine::getPositionId, obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        resultSecondExamineService.remove(queryWrapper3);

        UpdateWrapper<Position> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("audit_status", '0').eq("id", obj.getString("positionId"));
        positionService.update(updateWrapper);

        List<BackWait> backWaitList = backWaitService.lambdaQuery()
                .eq(BackWait::getPositionId, obj.getString("positionId"))
                .list();
        backWaitList.forEach(x -> {
            runtimeService.deleteProcessInstance(x.getProcessDefineId(), "删除原因");
            backWaitService.removeById(x);
        });

        return R.success();
    }


    @PostMapping("/userId")
    private R<List<IdentityLink>> searchId(String taskId) {
        List<IdentityLink> id = taskService.getIdentityLinksForTask(taskId);
        return R.success(id);
    }

    @PostMapping("/complete")
    private R<Void> complete(String id) {
        List<String> assessorList1 = new ArrayList<>(4);
        assessorList1.add("userOne");
        assessorList1.add("userTwo");
        List<String> assessorList2 = new ArrayList<>(4);
        assessorList2.add("userThree");
        Map<String, Object> map = new HashMap<>(32);
        map.put("appoint", "no");
        map.put("assessor", "xxy");
        map.put("declear", "q");
        map.put("pieceAppoint", "true");
        map.put("scoreAppoint", "false");
        map.put("kpiAppoint", "true");
        map.put("okrAppoint", "false");
        map.put("ASList", assessorList1);
        map.put("AKList", assessorList1);
        map.put("APList", assessorList2);
        map.put("AOList", assessorList2);
        map.put("AThirdList", assessorList2);
        taskService.complete(id, map);
        return R.success();
    }

    @PostMapping("/getComplete")
    private R<List<HistoricTaskInstance>> getCompleteList() {
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

}


