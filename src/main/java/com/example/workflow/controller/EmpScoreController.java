package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.model.entity.BackWait;
import com.example.workflow.model.entity.EmpScore;
import com.example.workflow.model.entity.EmpScoreView;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.PositionAssessor;
import com.example.workflow.model.entity.TaskView;
import com.example.workflow.model.pojo.EmpScoreExcel;
import com.example.workflow.service.BackWaitService;
import com.example.workflow.service.EmpScoreService;
import com.example.workflow.service.EmpScoreViewService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.PositionService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/EmpScore")
public class EmpScoreController {

    @Autowired
    private EmpScoreService EmpScoreService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private PositionService PositionService;
    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
            private EmpScoreViewService EmpScoreViewService;
    @Autowired
            private BackWaitService BackWaitService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @PostMapping("/downLoad")
    private void downLoad(HttpServletResponse response) throws IOException {
        List<EmpScoreView> list=EmpScoreViewService.lambdaQuery()
                .orderByAsc(EmpScoreView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        List<EmpScoreExcel> result=new ArrayList<>();
        list.forEach(x->{
            EmpScoreExcel one=new EmpScoreExcel();
            BeanUtils.copyProperties(x,one);
            result.add(one);
        });

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

        EasyExcel.write(response.getOutputStream(), EmpScoreExcel.class)
                .sheet("导出")
                .doWrite(result);
    }

    @PostMapping("/add")
    private R add(@RequestBody JSONObject form){
        JSONArray formArray = form.getJSONArray("Form");

        List<EmpScore> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            EmpScore one = new EmpScore();
            one.setScoreAssessorsId(Long.valueOf(String.valueOf(formObject.get("scoreAssessorsId"))));
            one.setEmpId(Long.valueOf(String.valueOf(formObject.get("empId"))));

            if(formObject.get("score").toString()==null||formObject.get("score").toString().isEmpty()){
                return R.error("评分不得为空");
            }
            if(new BigDecimal(formObject.get("score").toString()).compareTo(new BigDecimal(0)) < 0){
                return R.error("评分不得小于0");
            }
            if(new BigDecimal(formObject.get("score").toString()).compareTo(new BigDecimal(120)) > 0){
                return R.error("评分不得超过120");
            }

            one.setScore(new BigDecimal(formObject.get("score").toString()));
            list.add(one);
        }
        EmpScoreService.saveBatch(list);

        PositionAssessor nextAssessor= PositionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId,form.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        Map<String,Object> map = new HashMap<>();
        if(form.getString("positionType").equals("5")){
            map.put("thirdAssessor",nextAssessor.getThirdAssessorId().toString());
            map.put("thirdTimer",nextAssessor.getThirdTimer());
        }
        else if(form.getString("positionType").equals("4")){
            map.put("secondAssessor",nextAssessor.getSecondAssessorId().toString());
            map.put("secondTimer",nextAssessor.getSecondTimer());
        }

        LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .eq(EmployeePosition::getEmpId,form.getString("empId"));
        EmployeePosition EmployeePosition=EmployeePositionService.getOne(queryWrapper);

        String assessorId=form.getString("assessorId");
        String empId=form.getString("empId");
        LambdaQueryWrapper<TaskView> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(TaskView::getAssignee,assessorId)
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getName,"score")
                .eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId());
        TaskView task=TaskViewMapper.selectOne(queryWrapper2);

        taskService.complete(task.getId(),map);

        return R.success();
    }

    @GetMapping("/nowPage")
    public R<Page> nowPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize){

        Page<EmpScoreView> pageInfo=new Page<EmpScoreView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(EmpScoreView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);

        EmpScoreViewService.page(pageInfo,queryWrapper);


        return R.success(pageInfo);
    }


    @GetMapping("/nowSearch")
    public R<Page> nowSerach(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String empId
            ,@RequestParam(defaultValue = "") String empName){

        Page<EmpScoreView> pageInfo=new Page<EmpScoreView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(EmpScoreView::getEmpId,empId)
                .like(EmpScoreView::getEmpName,empName)
                .orderByAsc(EmpScoreView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpScoreViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @GetMapping("/pastPage")
    public R<Page> pastPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<EmpScoreView> pageInfo=new Page<EmpScoreView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(EmpScoreView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        EmpScoreViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/pastSearch")
    public R<Page> pastPage(@RequestParam("page") String page
            ,@RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String empId
            ,@RequestParam(defaultValue = "") String empName
            ,@RequestParam(defaultValue = "") String beginTime
            ,@RequestParam(defaultValue = "") String endTime){

        Page<EmpScoreView> pageInfo=new Page<EmpScoreView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpScoreView> queryWrapper=new LambdaQueryWrapper<>();
        if(beginTime.isEmpty()){
            LocalDate today = LocalDate.now();
            LocalDateTime beginDay = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

            queryWrapper.like(EmpScoreView::getEmpId,empId)
                    .like(EmpScoreView::getEmpName,empName)
                    .orderByAsc(EmpScoreView::getEmpId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay);
        }
        else {
            LocalDateTime beginDay = LocalDateTime.of(LocalDate.parse(beginTime), LocalTime.MIN);
            LocalDateTime endDay = LocalDateTime.of(LocalDate.parse(endTime), LocalTime.MAX);

            queryWrapper.like(EmpScoreView::getEmpId,empId)
                    .like(EmpScoreView::getEmpName,empName)
                    .orderByAsc(EmpScoreView::getEmpId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay)
                    .apply(StringUtils.checkValNotNull(endDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endDay);
        }
        EmpScoreViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/reAdd")
    private R reAdd(@RequestParam("id") String id
            ,@RequestParam("correctedValue") String correctedValue
            ,@RequestParam("empId") String empId){
        UpdateWrapper<EmpScore> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("id",id)
                .set("corrected_value",correctedValue);
        EmpScoreService.update(updateWrapper);

        EmpScoreService.reChange(Long.valueOf(id),Long.valueOf(empId));

        return R.success();
    }


    @PostMapping("/reDelete")
    private R reAdd(@RequestBody EmpScore one){
        one.setState(new Short("0"));
        EmpScoreService.updateById(one);

        EmpScoreService.reChange(one.getId(),one.getEmpId());
        return R.success();
    }


    @PostMapping("/reBackAdd")
    private R reBackAdd(@RequestBody JSONObject form){
        JSONArray formArray = form.getJSONArray("Form");

        List<EmpScore> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            EmpScore one = new EmpScore();
            one.setScoreAssessorsId(Long.valueOf(String.valueOf(formObject.get("scoreAssessorsId"))));
            one.setEmpId(Long.valueOf(String.valueOf(formObject.get("empId"))));
            if(formObject.get("score").toString()==null||formObject.get("score").toString().isEmpty()){
                return R.error("评分不得为空");
            }
            one.setScore(new BigDecimal(formObject.get("score").toString()));
            list.add(one);
        }
        EmpScoreService.saveBatch(list);

        PositionAssessor nextAssessor= PositionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId,form.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        Map<String,Object> map = new HashMap<>();
        if(form.getString("positionType").equals("5")){
            map.put("Assessor",nextAssessor.getThirdAssessorId().toString());
        }
        else if(form.getString("positionType").equals("4")||form.getString("positionType").equals("3")){
            map.put("Assessor",nextAssessor.getSecondAssessorId().toString());
        }

        List<BackWait> backWait=BackWaitService.lambdaQuery()
                .eq(BackWait::getEmpId,form.getString("empId"))
                .eq(BackWait::getPositionId,form.getString("positionId"))
                .and(qw -> qw.eq(BackWait::getType,"third_score_back")
                        .or().eq(BackWait::getType,"back_second_score"))
                .list();

        String assessorId=form.getString("assessorId");
        String empId=form.getString("empId");
        LambdaQueryWrapper<TaskView> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(TaskView::getAssignee,assessorId)
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getName,"back_score");
        List<TaskView> task=TaskViewMapper.selectList(queryWrapper2);

        backWait.forEach(x->{
            task.forEach(y->{
                System.out.println(y.getProcInstId().equals(x.getProcessDefineId()));
                if(y.getProcInstId().equals(x.getProcessDefineId())){
                    System.out.println("完成任务");
                    taskService.complete(y.getId(),map);
                }
            });
        });

        return R.success();
    }


    @PostMapping("/getOneEmpScore")
    private R<List<EmpScoreView>> getOneEmpOkr(@RequestBody JSONObject form){
        List<EmpScoreView> empScoreViewList= EmpScoreViewService.lambdaQuery()
                .eq(EmpScoreView::getEmpId,form.getString("empId"))
                .eq(EmpScoreView::getPositionId,form.getString("positionId"))
                .eq(EmpScoreView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(empScoreViewList);
    }


    @PostMapping("/secondBack")
    private R<List<EmpScoreView>> secondBack(@RequestBody JSONObject obj){
        List<EmpScoreView> empScoreViews=EmpScoreViewService.lambdaQuery()
                .eq(EmpScoreView::getPositionId,obj.getString("positionId"))
                .orderByAsc(EmpScoreView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(empScoreViews);
    }

}
