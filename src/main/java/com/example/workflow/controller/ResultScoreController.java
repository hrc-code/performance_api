package com.example.workflow.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.BackWait;
import com.example.workflow.entity.EmpScoreView;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.PositionAssessor;
import com.example.workflow.entity.ResultScore;
import com.example.workflow.entity.ResultScoreEmpView;
import com.example.workflow.entity.TaskView;
import com.example.workflow.mapper.EmpScoreContentViewMapper;
import com.example.workflow.mapper.ResultScoreEmpViewMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.service.BackWaitService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.PositionScoreService;
import com.example.workflow.service.ResultScoreService;
import com.example.workflow.service.ScoreAssessorsService;
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
@RequestMapping("/ResultScore")
public class ResultScoreController {
    @Autowired
    private EmpScoreContentViewMapper EmpScoreContentViewMapper;
    @Autowired
    private ResultScoreService ResultScoreService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private ResultScoreEmpViewMapper ResultScoreEmpViewMapper;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private BackWaitService BackWaitService;
    @Autowired
    private PositionScoreService PositionScoreService;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private ScoreAssessorsService ScoreAssessorsService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private PositionAssessorService PositionAssessorService;


    @PostMapping("/list")
    private R<List<EmpScoreView>> list(@RequestBody JSONObject obj){

        LambdaQueryWrapper<EmpScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpScoreView::getEmpId,obj.getString("empId"))
                .eq(EmpScoreView::getPositionId,obj.get("positionId"));
        List<EmpScoreView> list=EmpScoreContentViewMapper.selectList(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/add")
    private R add(@RequestBody JSONObject form){
        JSONArray formArray = form.getJSONArray("Form");

        List<ResultScore> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            ResultScore one = new ResultScore();
            one.setAssessorId(Long.valueOf(String.valueOf(formObject.get("assessorId"))));
            one.setEmpScoreId(Long.valueOf(String.valueOf(formObject.get("empScoreId"))));
            //one.setExamine(new Short(formObject.get("examine").toString()));
            one.setExamine(new Short("1"));
            list.add(one);
        }
        ResultScoreService.saveBatch(list);

        return R.success();
    }

    @PostMapping("/BackScoreList")
    private R<List<ResultScoreEmpView>> BackList(@RequestBody JSONObject obj){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

        LambdaQueryWrapper<ResultScoreEmpView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ResultScoreEmpView::getEmpId,obj.getString("empId"))
                .eq(ResultScoreEmpView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<ResultScoreEmpView> list=ResultScoreEmpViewMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/BackAdd")
    private R BackAdd(@RequestBody JSONObject form){
        /*JSONArray formArray = form.getJSONArray("Form");

        List<ResultScore> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            ResultScore one = new ResultScore();
            one.setAssessorId(Long.valueOf(String.valueOf(formObject.get("assessorId"))));
            one.setEmpScoreId(Long.valueOf(String.valueOf(formObject.get("empScoreId"))));
            one.setExamine(new Short(formObject.get("examine").toString()));
            list.add(one);
        }
        ResultScoreService.saveBatch(list);

        List<String> assessorList1 = new ArrayList<>();
        LambdaQueryWrapper<PositionScore> query1=new LambdaQueryWrapper<>();
        query1.eq(PositionScore::getPositionId,form.getString("positionId"));
        List<PositionScore> num1= PositionScoreService.list(query1);

        num1.forEach(y->{
            LambdaQueryWrapper<ScoreAssessors> query=new LambdaQueryWrapper<>();
            query.eq(ScoreAssessors::getPositionScoreId,y.getId());

            List<ScoreAssessors> assessor= ScoreAssessorsService.list(query);
            assessor.forEach(z->{
                assessorList1.add(String.valueOf(z.getAssessorId()));
            });
        });
        List<String> assessors1=assessorList1.stream().distinct().collect(Collectors.toList());

        Map<String,Object> map = new HashMap<>();
        identityService.setAuthenticatedUserId(form.getString("empId"));
        map.put("ASList",assessors1);
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("Process_12mkpug",map);
        BackWait one=BackWaitService.splitForm(form,processInstance,"Process_12mkpug");
        BackWaitService.save(one);

        String assessorId=form.getString("assessorId");
        String empId=form.getString("empId");

        LambdaQueryWrapper<EmployeePosition> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getEmpId,form.getString("empId"))
                .eq(EmployeePosition::getPositionId,form.getString("positionId"));
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper2);

        LambdaQueryWrapper<TaskView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee,assessorId)
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getName,"score");
        TaskView task=TaskViewMapper.selectOne(queryWrapper);
        runtimeService.suspendProcessInstanceById(task.getProcInstId());*/

        return R.success();
    }

    @PostMapping("/updateBackAdd")
    private R updateBackAdd(@RequestBody JSONObject form){
        JSONArray formArray = form.getJSONArray("Form");

        List<ResultScore> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            ResultScore one = new ResultScore();
            one.setId(Long.valueOf(String.valueOf(formObject.get("id"))));
            one.setAssessorId(Long.valueOf(String.valueOf(formObject.get("assessorId"))));
            one.setEmpScoreId(Long.valueOf(String.valueOf(formObject.get("empScoreId"))));
            one.setExamine(new Short(formObject.get("examine").toString()));
            list.add(one);
        }
        ResultScoreService.updateBatchById(list);

        LambdaQueryWrapper<BackWait> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(BackWait::getPositionId,form.getString("positionId"))
                .eq(BackWait::getEmpId,form.getString("empId"));
        BackWait backWait= BackWaitService.getOne(queryWrapper5);

        LambdaQueryWrapper<TaskView> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(TaskView::getAssignee,form.getString("assessorId"))
                .eq(TaskView::getStartUserId,form.getString("empId"))
                .eq(TaskView::getName,"back_third_score")
                .eq(TaskView::getProcInstId,backWait.getPositionId());
        TaskView task1=TaskViewMapper.selectOne(queryWrapper3);
        taskService.complete(task1.getId());

        String assessorId=form.getString("assessorId");
        String empId=form.getString("empId");
        LambdaQueryWrapper<EmployeePosition> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getEmpId,form.getString("empId"))
                .eq(EmployeePosition::getPositionId,form.getString("positionId"));
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper2);

        LambdaQueryWrapper<PositionAssessor> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(PositionAssessor::getPositionId,form.getString("positionId"));
        PositionAssessor nextAssessor= PositionAssessorService.getOne(queryWrapper1);

        Map<String,Object> map = new HashMap<>();
        map.put("secondAssessor",nextAssessor.getSecondAssessorId().toString());
        map.put("secondTimer",nextAssessor.getSecondTimer());

        LambdaQueryWrapper<TaskView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee,assessorId)
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getName,"third")
                .eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId());
        TaskView task=TaskViewMapper.selectOne(queryWrapper);
        runtimeService.activateProcessInstanceById(task.getProcInstId());
        taskService.complete(task.getId(),map);

        return R.success();
    }


}
