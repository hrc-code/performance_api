package com.example.workflow.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.ResultSecondExamineMapper;
import com.example.workflow.model.entity.BackWait;
import com.example.workflow.model.entity.Employee;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.OkrKey;
import com.example.workflow.model.entity.OkrRule;
import com.example.workflow.model.entity.PositionScore;
import com.example.workflow.model.entity.ResultSecondExamine;
import com.example.workflow.model.entity.Role;
import com.example.workflow.model.entity.ScoreAssessors;
import com.example.workflow.service.BackWaitService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.EmployeeService;
import com.example.workflow.service.OkrKeyService;
import com.example.workflow.service.OkrRuleService;
import com.example.workflow.service.PositionScoreService;
import com.example.workflow.service.ResultSecondExamineService;
import com.example.workflow.service.RoleService;
import com.example.workflow.service.ScoreAssessorsService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResultSecondExamineServiceImpl extends ServiceImpl<ResultSecondExamineMapper, ResultSecondExamine> implements ResultSecondExamineService {
    @Autowired
    private IdentityService identityService;
    @Autowired
    private EmployeePositionService employeePositionService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private PositionScoreService positionScoreService;
    @Autowired
    private ScoreAssessorsService scoreAssessorsService;
    @Autowired
    private BackWaitService backWaitService;
    @Autowired
    private OkrKeyService okrKeyService;
    @Autowired
    private OkrRuleService okrRuleService;
    @Autowired
    private EmployeeService employeeService;

    @Override
    public void rePieceSecondFifth(JSONObject form){
        Long roleId = roleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId = employeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        List<EmployeePosition> list = employeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String, Object> map = new HashMap<>(10);
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_1a4y3g9",map);

            BackWait backWait = backWaitService.addOne(
                    Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId()
                    , "back_second_piece"
                    ,processInstance
                    ,"Process_1a4y3g9"
                    ,form.getString("opinion2"));
            backWaitService.save(backWait);
        });
    }


    @Override
    public void reKpiSecondFifth(JSONObject form){
        Long roleId = roleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId = employeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();


        List<EmployeePosition> list = employeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String, Object> map = new HashMap<>(10);
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_065p3a1",map);

            BackWait backWait = backWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_kpi"
                    ,processInstance
                    ,"Process_065p3a1"
                    ,form.getString("opinion3"));
            backWaitService.save(backWait);
        });
    }


    @Override
    public void reScoreSecondFifth(JSONObject form){
        List<String> assessorList = new ArrayList<>(10);

        List<PositionScore> num = positionScoreService.lambdaQuery()
                .eq(PositionScore::getPositionId,form.getString("positionId"))
                .list();

        num.forEach(y->{
            List<ScoreAssessors> assessor = scoreAssessorsService.lambdaQuery()
                    .eq(ScoreAssessors::getPositionScoreId,y.getId())
                    .list();
            assessor.forEach(z -> assessorList.add(String.valueOf(z.getAssessorId())));
        });
        List<String> assessors=assessorList.stream().distinct().collect(Collectors.toList());

        List<EmployeePosition> list = employeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();
        System.out.println();
        list.forEach(x->{
            Map<String, Object> map = new HashMap<>(10);
            map.put("ASList", assessors);

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0scky72",map);

            BackWait backWait = backWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_score"
                    ,processInstance
                    ,"Process_0scky72"
                    ,form.getString("opinion1"));
            backWaitService.save(backWait);
        });
    }


    @Override
    public void reOkrSecondFifth(JSONObject form,EmployeePosition emp){
            List<String> assessorList = new ArrayList<>();

        List<OkrKey> num = okrKeyService.lambdaQuery()
                    .eq(OkrKey::getPositionId,form.getString("positionId"))
                    .eq(OkrKey::getLiaEmpId,emp.getEmpId())
                    .list();

            num.forEach(y->{
                List<OkrRule> ruleList = okrRuleService.lambdaQuery()
                        .eq(OkrRule::getId,y.getId())
                        .list();
                ruleList.forEach(z -> assessorList.add(String.valueOf(z.getAssessorId())));
            });
            List<String> assessors=assessorList.stream().distinct().collect(Collectors.toList());

        Map<String, Object> map = new HashMap<>(10);
            map.put("AOList", assessors);

            identityService.setAuthenticatedUserId(String.valueOf(emp.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0jeppqc",map);

        BackWait backWait = backWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,emp.getEmpId(),"back_second_okr"
                    ,processInstance
                    ,"Process_0jeppqc"
                    ,form.getString("opinion4"));
        backWaitService.save(backWait);
    }


    @Override
    public void rePieceSecondFourth(JSONObject form){
        Long roleId = roleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId = employeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        List<EmployeePosition> list = employeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String, Object> map = new HashMap<>(10);
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0a7afjy",map);

            BackWait backWait = backWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_piece"
                    ,processInstance
                    ,"Process_0a7afjy"
                    ,form.getString("opinion2"));
            backWaitService.save(backWait);
        });
    }


    @Override
    public void reKpiSecondFourth(JSONObject form){
        Long roleId = roleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId = employeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        List<EmployeePosition> list = employeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String, Object> map = new HashMap<>(10);
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0tcatx0",map);

            BackWait backWait = backWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_kpi"
                    ,processInstance
                    ,"Process_0tcatx0"
                    ,form.getString("opinion3"));
            backWaitService.save(backWait);
        });
    }


    @Override
    public void reScoreSecondFourth(JSONObject form){
        List<String> assessorList = new ArrayList<>();

        List<PositionScore> num = positionScoreService.lambdaQuery()
                .eq(PositionScore::getPositionId,form.getString("positionId"))
                .list();

        num.forEach(y->{
            List<ScoreAssessors> assessor = scoreAssessorsService.lambdaQuery()
                    .eq(ScoreAssessors::getPositionScoreId,y.getId())
                    .list();
            assessor.forEach(z -> assessorList.add(String.valueOf(z.getAssessorId())));
        });
        List<String> assessors=assessorList.stream().distinct().collect(Collectors.toList());

        List<EmployeePosition> list = employeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String, Object> map = new HashMap<>(10);
            map.put("ASList", assessors);

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_17y117z",map);

            BackWait backWait = backWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_score"
                    ,processInstance
                    ,"Process_17y117z"
                    ,form.getString("opinion1"));
            backWaitService.save(backWait);
        });
    }


    @Override
    public void reOkrSecondFourth(JSONObject form,EmployeePosition emp){
            List<String> assessorList = new ArrayList<>();

        List<OkrKey> num = okrKeyService.lambdaQuery()
                    .eq(OkrKey::getPositionId,form.getString("positionId"))
                    .eq(OkrKey::getLiaEmpId,emp.getEmpId())
                    .list();

            num.forEach(y->{
                List<OkrRule> ruleList = okrRuleService.lambdaQuery()
                        .eq(OkrRule::getId,y.getRuleId())
                        .list();
                ruleList.forEach(z -> assessorList.add(String.valueOf(z.getAssessorId())));
            });
            List<String> assessors=assessorList.stream().distinct().collect(Collectors.toList());

        Map<String, Object> map = new HashMap<>(10);
            map.put("AOList", assessors);

            identityService.setAuthenticatedUserId(String.valueOf(emp.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0aqalb8",map);

        BackWait backWait = backWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,emp.getEmpId(),"back_second_okr"
                    ,processInstance
                    ,"Process_0aqalb8"
                    ,form.getString("opinion4"));
        backWaitService.save(backWait);
    }


    @Override
    public void rePieceSecondThird(JSONObject form){
        Long roleId = roleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId = employeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        List<EmployeePosition> list = employeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String, Object> map = new HashMap<>(16);
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_1rgr65b",map);

            BackWait backWait = backWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_piece"
                    ,processInstance
                    ,"Process_1rgr65b"
                    ,form.getString("opinion2"));
            backWaitService.save(backWait);
        });
    }


    @Override
    public void reKpiSecondThird(JSONObject form){
        Long roleId = roleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId = employeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        List<EmployeePosition> list = employeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String, Object> map = new HashMap<>(32);
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0m9w7vi",map);

            BackWait backWait = backWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_kpi"
                    ,processInstance
                    ,"Process_0m9w7vi"
                    ,form.getString("opinion3"));
            backWaitService.save(backWait);
        });
    }
}
