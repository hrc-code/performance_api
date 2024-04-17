package com.example.workflow.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.BackWait;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.OkrKey;
import com.example.workflow.entity.OkrRule;
import com.example.workflow.entity.PositionScore;
import com.example.workflow.entity.ResultSecondExamine;
import com.example.workflow.entity.Role;
import com.example.workflow.entity.ScoreAssessors;
import com.example.workflow.mapper.ResultSecondExamineMapper;
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
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private RoleService RoleService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private PositionScoreService PositionScoreService;
    @Autowired
    private ScoreAssessorsService ScoreAssessorsService;
    @Autowired
    private BackWaitService BackWaitService;
    @Autowired
    private OkrKeyService OkrKeyService;
    @Autowired
    private OkrRuleService OkrRuleService;
    @Autowired
    private EmployeeService EmployeeService;

    @Override
    public void rePieceSecondFifth(JSONObject form){
        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        List<EmployeePosition> list= EmployeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String,Object> map = new HashMap<>();
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_1a4y3g9",map);

            BackWait backWait=BackWaitService.addOne(
                    Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId()
                    , "back_second_piece"
                    ,processInstance
                    ,"Process_1a4y3g9"
                    ,form.getString("opinion2"));
            BackWaitService.save(backWait);
        });
    }


    @Override
    public void reKpiSecondFifth(JSONObject form){
        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();


        List<EmployeePosition> list= EmployeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String,Object> map = new HashMap<>();
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_065p3a1",map);

            BackWait backWait=BackWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_kpi"
                    ,processInstance
                    ,"Process_065p3a1"
                    ,form.getString("opinion3"));
            BackWaitService.save(backWait);
        });
    }


    @Override
    public void reScoreSecondFifth(JSONObject form){
        List<String> assessorList = new ArrayList<>();

        List<PositionScore> num= PositionScoreService.lambdaQuery()
                .eq(PositionScore::getPositionId,form.getString("positionId"))
                .list();

        num.forEach(y->{
            List<ScoreAssessors> assessor= ScoreAssessorsService.lambdaQuery()
                    .eq(ScoreAssessors::getPositionScoreId,y.getId())
                    .list();
            assessor.forEach(z->{
                assessorList.add(String.valueOf(z.getAssessorId()));
            });
        });
        List<String> assessors=assessorList.stream().distinct().collect(Collectors.toList());

        List<EmployeePosition> list= EmployeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String,Object> map = new HashMap<>();
            map.put("ASList", assessors);

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0scky72",map);

            BackWait backWait=BackWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_score"
                    ,processInstance
                    ,"Process_0scky72"
                    ,form.getString("opinion1"));
            BackWaitService.save(backWait);
        });
    }


    @Override
    public void reOkrSecondFifth(JSONObject form,EmployeePosition emp){
            List<String> assessorList = new ArrayList<>();

            List<OkrKey> num= OkrKeyService.lambdaQuery()
                    .eq(OkrKey::getPositionId,form.getString("positionId"))
                    .eq(OkrKey::getLiaEmpId,emp.getEmpId())
                    .list();

            num.forEach(y->{
                List<OkrRule> ruleList= OkrRuleService.lambdaQuery()
                        .eq(OkrRule::getId,y.getId())
                        .list();
                ruleList.forEach(z->{
                    assessorList.add(String.valueOf(z.getAssessorId()));
                });
            });
            List<String> assessors=assessorList.stream().distinct().collect(Collectors.toList());

            Map<String,Object> map = new HashMap<>();
            map.put("AOList", assessors);

            identityService.setAuthenticatedUserId(String.valueOf(emp.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0jeppqc",map);

            BackWait backWait=BackWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,emp.getEmpId(),"back_second_okr"
                    ,processInstance
                    ,"Process_0jeppqc"
                    ,form.getString("opinion4"));
            BackWaitService.save(backWait);
    }


    @Override
    public void rePieceSecondFourth(JSONObject form){
        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        List<EmployeePosition> list= EmployeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String,Object> map = new HashMap<>();
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0a7afjy",map);

            BackWait backWait=BackWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_piece"
                    ,processInstance
                    ,"Process_0a7afjy"
                    ,form.getString("opinion2"));
            BackWaitService.save(backWait);
        });
    }


    @Override
    public void reKpiSecondFourth(JSONObject form){
        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        List<EmployeePosition> list= EmployeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String,Object> map = new HashMap<>();
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0tcatx0",map);

            BackWait backWait=BackWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_kpi"
                    ,processInstance
                    ,"Process_0tcatx0"
                    ,form.getString("opinion3"));
            BackWaitService.save(backWait);
        });
    }


    @Override
    public void reScoreSecondFourth(JSONObject form){
        List<String> assessorList = new ArrayList<>();

        List<PositionScore> num= PositionScoreService.lambdaQuery()
                .eq(PositionScore::getPositionId,form.getString("positionId"))
                .list();

        num.forEach(y->{
            List<ScoreAssessors> assessor= ScoreAssessorsService.lambdaQuery()
                    .eq(ScoreAssessors::getPositionScoreId,y.getId())
                    .list();
            assessor.forEach(z->{
                assessorList.add(String.valueOf(z.getAssessorId()));
            });
        });
        List<String> assessors=assessorList.stream().distinct().collect(Collectors.toList());

        List<EmployeePosition> list= EmployeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String,Object> map = new HashMap<>();
            map.put("ASList", assessors);

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_17y117z",map);

            BackWait backWait=BackWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_score"
                    ,processInstance
                    ,"Process_17y117z"
                    ,form.getString("opinion1"));
            BackWaitService.save(backWait);
        });
    }


    @Override
    public void reOkrSecondFourth(JSONObject form,EmployeePosition emp){
            List<String> assessorList = new ArrayList<>();

            List<OkrKey> num= OkrKeyService.lambdaQuery()
                    .eq(OkrKey::getPositionId,form.getString("positionId"))
                    .eq(OkrKey::getLiaEmpId,emp.getEmpId())
                    .list();

            num.forEach(y->{
                List<OkrRule> ruleList= OkrRuleService.lambdaQuery()
                        .eq(OkrRule::getId,y.getRuleId())
                        .list();
                ruleList.forEach(z->{
                    assessorList.add(String.valueOf(z.getAssessorId()));
                });
            });
            List<String> assessors=assessorList.stream().distinct().collect(Collectors.toList());

            Map<String,Object> map = new HashMap<>();
            map.put("AOList", assessors);

            identityService.setAuthenticatedUserId(String.valueOf(emp.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0aqalb8",map);

            BackWait backWait=BackWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,emp.getEmpId(),"back_second_okr"
                    ,processInstance
                    ,"Process_0aqalb8"
                    ,form.getString("opinion4"));
            BackWaitService.save(backWait);
    }


    @Override
    public void rePieceSecondThird(JSONObject form){
        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        List<EmployeePosition> list= EmployeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String,Object> map = new HashMap<>();
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_1rgr65b",map);

            BackWait backWait=BackWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_piece"
                    ,processInstance
                    ,"Process_1rgr65b"
                    ,form.getString("opinion2"));
            BackWaitService.save(backWait);
        });
    }


    @Override
    public void reKpiSecondThird(JSONObject form){
        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        List<EmployeePosition> list= EmployeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .list();

        list.forEach(x->{
            Map<String,Object> map = new HashMap<>();
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(x.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0m9w7vi",map);

            BackWait backWait=BackWaitService.addOne(Long.valueOf(form.getString("positionId"))
                    ,x.getEmpId(),"back_second_kpi"
                    ,processInstance
                    ,"Process_0m9w7vi"
                    ,form.getString("opinion3"));
            BackWaitService.save(backWait);
        });
    }
}
