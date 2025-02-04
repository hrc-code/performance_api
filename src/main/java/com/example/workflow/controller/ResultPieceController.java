package com.example.workflow.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.mapper.EmpPieceViewMapper;
import com.example.workflow.mapper.EmployeeMapper;
import com.example.workflow.mapper.ResultPieceEmpViewMapper;
import com.example.workflow.mapper.RoleMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.model.entity.BackWait;
import com.example.workflow.model.entity.EmpPieceView;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.PositionAssessor;
import com.example.workflow.model.entity.ResultPiece;
import com.example.workflow.model.entity.ResultPieceEmpView;
import com.example.workflow.model.entity.TaskView;
import com.example.workflow.service.BackWaitService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.ResultPieceService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
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
@RequestMapping("/ResultPiece")
public class ResultPieceController {
    @Autowired
    private EmpPieceViewMapper EmpPieceViewMapper;
    @Autowired
    private ResultPieceService ResultPieceService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ResultPieceEmpViewMapper ResultPieceEmpViewMapper;
    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RoleMapper RoleMapper;
    @Autowired
    private BackWaitService BackWaitService;
    @Autowired
    private EmployeeMapper EmployeeMapper;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @PostMapping("/list")
    private R<List<EmpPieceView>> list(@RequestBody JSONObject obj){

        LambdaQueryWrapper<EmpPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpPieceView::getEmpId,obj.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpPieceView> list=EmpPieceViewMapper.selectList(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/add")
    private R add(@RequestBody JSONObject form){
        JSONArray formArray = form.getJSONArray("Form");

        List<ResultPiece> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            ResultPiece one = new ResultPiece();
            one.setEmpPieceId(Long.valueOf(String.valueOf(formObject.get("empPieceId"))));
            one.setAssessorId(Long.valueOf(String.valueOf(formObject.get("assessorId"))));
            one.setExamine(new Short(formObject.get("examine").toString()));
            if(formObject.get("examine").toString().equals("0")){
                return R.error("审核结果包含“不属实”条目，请选择驳回");
            }
            list.add(one);
        }
        ResultPieceService.saveBatch(list);

        LambdaQueryWrapper<PositionAssessor> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(PositionAssessor::getPositionId,form.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        PositionAssessor nextAssessor= PositionAssessorService.getOne(queryWrapper1);

        Map<String,Object> map = new HashMap<>();
        if(form.getString("positionType").equals("5")){
            map.put("thirdAssessor",nextAssessor.getThirdAssessorId().toString());
            map.put("thirdTimer",nextAssessor.getThirdTimer());
        }
        else if(form.getString("positionType").equals("4")){
            map.put("secondAssessor",nextAssessor.getSecondAssessorId().toString());
            map.put("secondTimer",nextAssessor.getSecondTimer());
        }

        LambdaQueryWrapper<EmployeePosition> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getEmpId,form.getString("empId"))
                .eq(EmployeePosition::getPositionId,form.getString("positionId"));
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper2);


        String assessorId=form.getString("assessorId");
        String empId=form.getString("empId");

        LambdaQueryWrapper<TaskView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee,assessorId)
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getName,"piece")
                .eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId());
        TaskView task=TaskViewMapper.selectOne(queryWrapper);

        taskService.complete(task.getId(),map);
        return R.success();
    }

    @PostMapping("/BackPieceList")
    private R<List<ResultPieceEmpView>> BackList(@RequestBody JSONObject obj){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

        LambdaQueryWrapper<ResultPieceEmpView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ResultPieceEmpView::getEmpId,obj.getString("empId"))
                .eq(ResultPieceEmpView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<ResultPieceEmpView> list=ResultPieceEmpViewMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/BackAdd")
    private R BackAdd(@RequestBody JSONObject form){
        /*JSONArray formArray = form.getJSONArray("Form");

        List<ResultPiece> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            ResultPiece one = new ResultPiece();
            one.setEmpPieceId(Long.valueOf(String.valueOf(formObject.get("empPieceId"))));
            one.setAssessorId(Long.valueOf(String.valueOf(formObject.get("assessorId"))));
            one.setExamine(new Short(formObject.get("examine").toString()));
            list.add(one);
        }
        ResultPieceService.saveBatch(list);*/

        /*LambdaQueryWrapper<Role> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1);
        Long roleId= RoleMapper.selectOne(queryWrapper3).getId();

        LambdaQueryWrapper<Employee> queryWrapper4=new LambdaQueryWrapper<>();
        queryWrapper4.eq(Employee::getRoleId,roleId);
        Long wageEmpId=EmployeeMapper.selectOne(queryWrapper4).getId();

        Map<String,Object> map = new HashMap<>();
        if(form.getString("positionType").equals("5")){
            identityService.setAuthenticatedUserId(form.getString("empId"));
            map.put("wageEmp",wageEmpId.toString());
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_049b9ah",map);
            BackWait one=BackWaitService.splitForm(form,processInstance,"Process_049b9ah");
            BackWaitService.save(one);
        }
        else if(form.getString("positionType").equals("4")){
            identityService.setAuthenticatedUserId(form.getString("empId"));
            map.put("wageEmp",wageEmpId.toString());
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0qrsna2",map);
            BackWait one=BackWaitService.splitForm(form,processInstance,"Process_0qrsna2");
            BackWaitService.save(one);
        }

        LambdaQueryWrapper<EmployeePosition> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getEmpId,form.getString("empId"))
                .eq(EmployeePosition::getPositionId,form.getString("positionId"));
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper2);

        String assessorId=form.getString("assessorId");
        String empId=form.getString("empId");
        LambdaQueryWrapper<TaskView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee,assessorId)
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getName,"piece")
                .eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId());
        TaskView task=TaskViewMapper.selectOne(queryWrapper);

        runtimeService.suspendProcessInstanceById(task.getProcInstId());*/

        return R.success();
    }

    @PostMapping("/updateBackAdd")
    private R updateBackAdd(@RequestBody JSONObject form){
        JSONArray formArray = form.getJSONArray("Form");

        List<ResultPiece> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            ResultPiece one = new ResultPiece();
            one.setId(Long.valueOf(String.valueOf(formObject.get("id"))));
            one.setEmpPieceId(Long.valueOf(String.valueOf(formObject.get("empPieceId"))));
            one.setAssessorId(Long.valueOf(String.valueOf(formObject.get("assessorId"))));
            one.setExamine(new Short(formObject.get("examine").toString()));
            list.add(one);
        }
        ResultPieceService.updateBatchById(list);

        LambdaQueryWrapper<PositionAssessor> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(PositionAssessor::getPositionId,form.getString("positionId"));
        PositionAssessor nextAssessor= PositionAssessorService.getOne(queryWrapper1);

        Map<String,Object> map = new HashMap<>();
        if(form.getString("positionType").equals("5")){
            map.put("thirdAssessor",nextAssessor.getThirdAssessorId().toString());
            map.put("thirdTimer",nextAssessor.getThirdTimer());
        }
        else if(form.getString("positionType").equals("4")){
            map.put("secondAssessor",nextAssessor.getSecondAssessorId().toString());
            map.put("secondTimer",nextAssessor.getSecondTimer());
        }

        String assessorId=form.getString("assessorId");
        String empId=form.getString("empId");

        LambdaQueryWrapper<BackWait> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(BackWait::getPositionId,form.getString("positionId"))
                .eq(BackWait::getEmpId,empId);
        BackWait backWait= BackWaitService.getOne(queryWrapper5);

        LambdaQueryWrapper<TaskView> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(TaskView::getAssignee,assessorId)
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getName,"back_fourth_piece")
                .eq(TaskView::getProcInstId,backWait.getPositionId());
        TaskView task1=TaskViewMapper.selectOne(queryWrapper3);
        taskService.complete(task1.getId(),map);


        return R.success();
    }
}
