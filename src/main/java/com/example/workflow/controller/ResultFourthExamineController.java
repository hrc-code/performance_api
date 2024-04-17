package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.BackWait;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.PositionAssessor;
import com.example.workflow.entity.ResultFourthExamine;
import com.example.workflow.entity.Role;
import com.example.workflow.entity.TaskView;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.service.BackWaitService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.EmployeeService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.ResultFourthExamineService;
import com.example.workflow.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ResultFourthExamine")
public class ResultFourthExamineController {
    @Autowired
    private ResultFourthExamineService ResultFourthExamineService;
    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
            private IdentityService identityService;
    @Autowired
            private RuntimeService runtimeService;
    @Autowired
            private RoleService RoleService;
    @Autowired
            private EmployeeService EmployeeService;
    @Autowired
            private BackWaitService BackWaitService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @PostMapping("/addPiece")
    private R add(@RequestBody JSONObject form) {
        LambdaQueryWrapper<ResultFourthExamine> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ResultFourthExamine::getPositionId, form.getString("positionId"))
                .eq(ResultFourthExamine::getEmpId, form.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ResultFourthExamine examine = ResultFourthExamineService.getOne(queryWrapper);

        if(examine==null){
            ResultFourthExamine newOne = new ResultFourthExamine();
            newOne.setPieceExamine(new Short("1"));
            newOne.setEmpId(Long.valueOf(form.getString("empId")));
            newOne.setPositionId(Long.valueOf(form.getString("positionId")));
            newOne.setAssessorId(Long.valueOf(form.getString("assessorId")));
            ResultFourthExamineService.save(newOne);

            if(!updatePiece(form)){
                R.error("该审核已被强制暂停，请退出审核，稍后重试");
            }
        }
        else {
            UpdateWrapper<ResultFourthExamine> updateWrapper=new UpdateWrapper<>();
            examine.setPieceExamine(new Short("1"));
            ResultFourthExamineService.updateById(examine);

            if(!updatePiece(form)){
                R.error("该审核已被强制暂停，请退出审核，稍后重试");
            }
        }

        return R.success();
    }


    @PostMapping("/addKpi")
    private R addKpi(@RequestBody JSONObject form) {
        LambdaQueryWrapper<ResultFourthExamine> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ResultFourthExamine::getPositionId, form.getString("positionId"))
                .eq(ResultFourthExamine::getEmpId, form.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ResultFourthExamine examine = ResultFourthExamineService.getOne(queryWrapper);

        if(examine==null){
            ResultFourthExamine newOne = new ResultFourthExamine();
            newOne.setKpiExamine(new Short("1"));
            newOne.setEmpId(Long.valueOf(form.getString("empId")));
            newOne.setPositionId(Long.valueOf(form.getString("positionId")));
            newOne.setAssessorId(Long.valueOf(form.getString("assessorId")));
            ResultFourthExamineService.save(newOne);

            if(!updateKpi(form)){
                R.error("该审核已被强制暂停，请退出审核，稍后重试");
            }

        }
        else {
            UpdateWrapper<ResultFourthExamine> updateWrapper=new UpdateWrapper<>();
            examine.setKpiExamine(new Short("1"));
            ResultFourthExamineService.updateById(examine);

            if(!updateKpi(form)){
                R.error("该审核已被强制暂停，请退出审核，稍后重试");
            }
        }

        return R.success();
    }


    @PostMapping("/rejectPiece")
    private R rejectPiece(@RequestBody JSONObject form){
        LambdaQueryWrapper<ResultFourthExamine> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ResultFourthExamine::getPositionId, form.getString("positionId"))
                .eq(ResultFourthExamine::getEmpId, form.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ResultFourthExamine examine = ResultFourthExamineService.getOne(queryWrapper);

        if(examine==null){
            ResultFourthExamine newOne = new ResultFourthExamine();
            newOne.setPieceExamine(new Short("2"));
            newOne.setEmpId(Long.valueOf(form.getString("empId")));
            newOne.setPositionId(Long.valueOf(form.getString("positionId")));
            newOne.setAssessorId(Long.valueOf(form.getString("assessorId")));
            ResultFourthExamineService.save(newOne);
        }
        else {
            UpdateWrapper<ResultFourthExamine> updateWrapper=new UpdateWrapper<>();
            examine.setPieceExamine(new Short("2"));
            ResultFourthExamineService.updateById(examine);
        }

        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();
        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        Map<String,Object> map = new HashMap<>();
        map.put("wageEmp",empId.toString());
        identityService.setAuthenticatedUserId(form.getString("empId"));
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("Process_049b9ah",map);

        BackWait backWait=new BackWait();
        backWait.setProcessDefineId(processInstance.getId());
        backWait.setType("fourth_piece_back");
        backWait.setEmpId(Long.valueOf(form.getString("empId")));
        backWait.setPositionId(Long.valueOf(form.getString("positionId")));
        backWait.setProcessKey("Process_049b9ah");
        backWait.setOpinion(form.getString("opinion"));
        BackWaitService.save(backWait);

        EmployeePosition employeePosition=EmployeePositionService.lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .eq(EmployeePosition::getEmpId,form.getString("empId"))
                .eq(EmployeePosition::getState,1)
                .one();

        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getStartUserId,form.getString("empId"))
                .eq(TaskView::getProcInstId,employeePosition.getProcessDefinitionId())
                .eq(TaskView::getName,"piece")
                .eq(TaskView::getAssignee,form.getString("assessorId"))
                .eq(TaskView::getState,"ACTIVE");

        TaskView task=TaskViewMapper.selectOne(Wrapper);
        if(task!=null){
            runtimeService.suspendProcessInstanceById(task.getProcInstId());
        }

        return R.success();
    }


    @PostMapping("/rejectKpi")
    private R rejectKpi(@RequestBody JSONObject form){
        LambdaQueryWrapper<ResultFourthExamine> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ResultFourthExamine::getPositionId, form.getString("positionId"))
                .eq(ResultFourthExamine::getEmpId, form.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ResultFourthExamine examine = ResultFourthExamineService.getOne(queryWrapper);

        if(examine==null){
            ResultFourthExamine newOne = new ResultFourthExamine();
            newOne.setKpiExamine(new Short("2"));
            newOne.setEmpId(Long.valueOf(form.getString("empId")));
            newOne.setPositionId(Long.valueOf(form.getString("positionId")));
            newOne.setAssessorId(Long.valueOf(form.getString("assessorId")));
            ResultFourthExamineService.save(newOne);
        }
        else {
            UpdateWrapper<ResultFourthExamine> updateWrapper=new UpdateWrapper<>();
            examine.setKpiExamine(new Short("2"));
            ResultFourthExamineService.updateById(examine);
        }

        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();
        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        Map<String,Object> map = new HashMap<>();
        map.put("wageEmp",empId.toString());
        identityService.setAuthenticatedUserId(form.getString("empId"));
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("Process_1fqcjpm",map);

        BackWait backWait=new BackWait();
        backWait.setProcessDefineId(processInstance.getId());
        backWait.setType("fourth_kpi_back");
        backWait.setEmpId(Long.valueOf(form.getString("empId")));
        backWait.setPositionId(Long.valueOf(form.getString("positionId")));
        backWait.setProcessKey("Process_1fqcjpm");
        backWait.setOpinion(form.getString("opinion"));
        BackWaitService.save(backWait);

        EmployeePosition employeePosition=EmployeePositionService.lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .eq(EmployeePosition::getEmpId,form.getString("empId"))
                .eq(EmployeePosition::getState,1)
                .one();

        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getStartUserId,form.getString("empId"))
                .eq(TaskView::getProcInstId,employeePosition.getProcessDefinitionId())
                .eq(TaskView::getName,"kpi")
                .eq(TaskView::getAssignee,form.getString("assessorId"))
                .eq(TaskView::getState,"ACTIVE");

        TaskView task=TaskViewMapper.selectOne(Wrapper);
        if(task!=null){
            runtimeService.suspendProcessInstanceById(task.getProcInstId());
        }

        return R.success();
    }


    @PostMapping("/reAddPiece")
    private R reAddPiece(@RequestBody JSONObject form) {
        LambdaQueryWrapper<ResultFourthExamine> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ResultFourthExamine::getPositionId, form.getString("positionId"))
                .eq(ResultFourthExamine::getEmpId, form.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ResultFourthExamine examine = ResultFourthExamineService.getOne(queryWrapper);
        examine.setPieceExamine(new Short("1"));
        ResultFourthExamineService.updateById(examine);

        LambdaQueryWrapper<BackWait> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(BackWait::getPositionId,form.getString("positionId"))
                .eq(BackWait::getEmpId,form.getString("empId"))
                .eq(BackWait::getType,"fourth_piece_back");
        BackWait backWait=BackWaitService.getOne(queryWrapper1);

        if(backWait!=null){
            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,form.getString("empId"))
                    .eq(TaskView::getProcInstId,backWait.getProcessDefineId())
                    .eq(TaskView::getName,"back_fourth_piece")
                    .eq(TaskView::getAssignee,form.getString("assessorId"))
                    .eq(TaskView::getState,"ACTIVE");
            TaskView task=TaskViewMapper.selectOne(Wrapper);
            taskService.complete(task.getId());

            BackWaitService.removeById(backWait);

            if(!examine.getKpiExamine().equals(new Short("2"))){
                if(!reUpdatePiece(form)){
                    R.error("该审核已被强制暂停，请退出审核，稍后重试");
                }
            }
        }

        LambdaQueryWrapper<BackWait> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(BackWait::getPositionId,form.getString("positionId"))
                .eq(BackWait::getEmpId,form.getString("empId"))
                .and(qw -> qw.eq(BackWait::getType,"third_piece_back")
                        .or().eq(BackWait::getType,"back_second_piece"));
        BackWait backThirdWait=BackWaitService.getOne(queryWrapper2);

        if(backThirdWait!=null){
            PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                    .eq(PositionAssessor::getPositionId, form.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .one();

            Map<String, Object> map = new HashMap<>();
            if (form.getString("positionType").equals("5")) {
                map.put("Assessor", nextAssessor.getThirdAssessorId().toString());
            } else if (form.getString("positionType").equals("4")) {
                map.put("Assessor", nextAssessor.getSecondAssessorId().toString());
            }

            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,form.getString("empId"))
                    .eq(TaskView::getProcInstId,backThirdWait.getProcessDefineId())
                    .eq(TaskView::getName,"back_fourth_piece")
                    .eq(TaskView::getAssignee,form.getString("assessorId"))
                    .eq(TaskView::getState,"ACTIVE");
            TaskView task=TaskViewMapper.selectOne(Wrapper);
            taskService.complete(task.getId(),map);
        }

        return R.success();
    }


    @PostMapping("/reAddKpi")
    private R reAddKpi(@RequestBody JSONObject form) {
        LambdaQueryWrapper<ResultFourthExamine> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ResultFourthExamine::getPositionId, form.getString("positionId"))
                .eq(ResultFourthExamine::getEmpId, form.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ResultFourthExamine examine = ResultFourthExamineService.getOne(queryWrapper);
        examine.setKpiExamine(new Short("1"));
        ResultFourthExamineService.updateById(examine);

        LambdaQueryWrapper<BackWait> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(BackWait::getPositionId,form.getString("positionId"))
                .eq(BackWait::getEmpId,form.getString("empId"))
                .eq(BackWait::getType,"fourth_kpi_back");
        BackWait backWait=BackWaitService.getOne(queryWrapper1);

        if(backWait!=null){
            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,form.getString("empId"))
                    .eq(TaskView::getProcInstId,backWait.getProcessDefineId())
                    .eq(TaskView::getName,"back_fourth_kpi")
                    .eq(TaskView::getAssignee,form.getString("assessorId"))
                    .eq(TaskView::getState,"ACTIVE");
            TaskView task=TaskViewMapper.selectOne(Wrapper);
            taskService.complete(task.getId());

            BackWaitService.removeById(backWait);

            if(!examine.getPieceExamine().equals(new Short("2"))){
                if(!reUpdateKpi(form)){
                    R.error("该审核已被强制暂停，请退出审核，稍后重试");
                }
            }
        }

        LambdaQueryWrapper<BackWait> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(BackWait::getPositionId,form.getString("positionId"))
                .eq(BackWait::getEmpId,form.getString("empId"))
                .and(qw -> qw.eq(BackWait::getType,"third_kpi_back")
                        .or().eq(BackWait::getType,"back_second_kpi"));
        BackWait backThirdWait=BackWaitService.getOne(queryWrapper2);

        if(backThirdWait!=null){
            PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                    .eq(PositionAssessor::getPositionId, form.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .one();

            Map<String, Object> map = new HashMap<>();
            if (form.getString("positionType").equals("5")) {
                map.put("Assessor", nextAssessor.getThirdAssessorId().toString());
            } else if (form.getString("positionType").equals("4")) {
                map.put("Assessor", nextAssessor.getSecondAssessorId().toString());
            }

            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,form.getString("empId"))
                    .eq(TaskView::getProcInstId,backThirdWait.getProcessDefineId())
                    .eq(TaskView::getName,"back_fourth_kpi")
                    .eq(TaskView::getAssignee,form.getString("assessorId"))
                    .eq(TaskView::getState,"ACTIVE");
            TaskView task=TaskViewMapper.selectOne(Wrapper);
            taskService.complete(task.getId(),map);
        }

        return R.success();
    }

    private Boolean updatePiece(JSONObject form) {
        PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId, form.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        Map<String, Object> map = new HashMap<>();
        if (form.getString("positionType").equals("5")) {
            map.put("thirdAssessor", nextAssessor.getThirdAssessorId().toString());
            map.put("thirdTimer", nextAssessor.getThirdTimer());
        } else if (form.getString("positionType").equals("4")) {
            map.put("secondAssessor", nextAssessor.getSecondAssessorId().toString());
            map.put("secondTimer", nextAssessor.getSecondTimer());
        }

        LambdaQueryWrapper<EmployeePosition> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getEmpId, form.getString("empId"))
                .eq(EmployeePosition::getPositionId, form.getString("positionId"));
        EmployeePosition EmployeePosition = EmployeePositionService.getOne(queryWrapper2);

        String assessorId = form.getString("assessorId");
        String empId = form.getString("empId");

        LambdaQueryWrapper<TaskView> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee, assessorId)
                .eq(TaskView::getStartUserId, empId)
                .eq(TaskView::getName, "piece")
                .eq(TaskView::getProcInstId, EmployeePosition.getProcessDefinitionId());
        TaskView task = TaskViewMapper.selectOne(queryWrapper);

        if (task != null) {
            try {
                taskService.complete(task.getId(), map);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private Boolean updateKpi(JSONObject form) {

        PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId, form.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        Map<String, Object> map = new HashMap<>();
        if (form.getString("positionType").equals("5")) {
            map.put("thirdAssessor", nextAssessor.getThirdAssessorId().toString());
            map.put("thirdTimer", nextAssessor.getThirdTimer());
        } else if (form.getString("positionType").equals("4")) {
            map.put("secondAssessor", nextAssessor.getSecondAssessorId().toString());
            map.put("secondTimer", nextAssessor.getSecondTimer());
        }

        LambdaQueryWrapper<EmployeePosition> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getEmpId, form.getString("empId"))
                .eq(EmployeePosition::getPositionId, form.getString("positionId"));
        EmployeePosition EmployeePosition = EmployeePositionService.getOne(queryWrapper2);

        String assessorId = form.getString("assessorId");
        String empId = form.getString("empId");

        LambdaQueryWrapper<TaskView> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee, assessorId)
                .eq(TaskView::getStartUserId, empId)
                .eq(TaskView::getName, "kpi")
                .eq(TaskView::getProcInstId, EmployeePosition.getProcessDefinitionId());
        TaskView task = TaskViewMapper.selectOne(queryWrapper);

        if (task != null) {
            try {
                taskService.complete(task.getId(), map);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private Boolean reUpdatePiece(JSONObject form) {
        PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId, form.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        Map<String, Object> map = new HashMap<>();
        if (form.getString("positionType").equals("5")) {
            map.put("thirdAssessor", nextAssessor.getThirdAssessorId().toString());
            map.put("thirdTimer", nextAssessor.getThirdTimer());
        } else if (form.getString("positionType").equals("4")) {
            map.put("secondAssessor", nextAssessor.getSecondAssessorId().toString());
            map.put("secondTimer", nextAssessor.getSecondTimer());
        }

        LambdaQueryWrapper<EmployeePosition> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getEmpId, form.getString("empId"))
                .eq(EmployeePosition::getPositionId, form.getString("positionId"));
        EmployeePosition EmployeePosition = EmployeePositionService.getOne(queryWrapper2);

        String assessorId = form.getString("assessorId");
        String empId = form.getString("empId");

        LambdaQueryWrapper<TaskView> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee, assessorId)
                .eq(TaskView::getStartUserId, empId)
                .eq(TaskView::getName, "piece")
                .eq(TaskView::getProcInstId, EmployeePosition.getProcessDefinitionId());
        TaskView task = TaskViewMapper.selectOne(queryWrapper);

        if (task != null) {
            try {
                runtimeService.activateProcessInstanceById(task.getProcInstId());
                taskService.complete(task.getId(), map);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private Boolean reUpdateKpi(JSONObject form) {
        PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId, form.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        Map<String, Object> map = new HashMap<>();
        if (form.getString("positionType").equals("5")) {
            map.put("thirdAssessor", nextAssessor.getThirdAssessorId().toString());
            map.put("thirdTimer", nextAssessor.getThirdTimer());
        } else if (form.getString("positionType").equals("4")) {
            map.put("secondAssessor", nextAssessor.getSecondAssessorId().toString());
            map.put("secondTimer", nextAssessor.getSecondTimer());
        }

        LambdaQueryWrapper<EmployeePosition> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getEmpId, form.getString("empId"))
                .eq(EmployeePosition::getPositionId, form.getString("positionId"));
        EmployeePosition EmployeePosition = EmployeePositionService.getOne(queryWrapper2);

        String assessorId = form.getString("assessorId");
        String empId = form.getString("empId");

        LambdaQueryWrapper<TaskView> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee, assessorId)
                .eq(TaskView::getStartUserId, empId)
                .eq(TaskView::getName, "kpi")
                .eq(TaskView::getProcInstId, EmployeePosition.getProcessDefinitionId());
        TaskView task = TaskViewMapper.selectOne(queryWrapper);

        if (task != null) {
            try {
                runtimeService.activateProcessInstanceById(task.getProcInstId());
                taskService.complete(task.getId(), map);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
