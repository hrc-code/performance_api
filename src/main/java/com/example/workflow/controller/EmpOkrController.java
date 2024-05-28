package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.entity.*;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.pojo.EmpOkrExcel;
import com.example.workflow.pojo.ResultEmpKpiExcel;
import com.example.workflow.service.BackWaitService;
import com.example.workflow.service.EmpOkrService;
import com.example.workflow.service.EmpOkrViewService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.OkrViewService;
import com.example.workflow.service.PositionAssessorService;
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
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/EmpOkr")
public class EmpOkrController {
    @Autowired
    private EmpOkrService EmpOkrService;
    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private EmpOkrViewService EmpOkrViewService;
    @Autowired
            private OkrViewService OkrViewService;
    @Autowired
            private BackWaitService BackWaitService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @PostMapping("/downLoad")
    private void downLoad(HttpServletResponse response) throws IOException {
        List<EmpOkrView> list=EmpOkrViewService.lambdaQuery()
                .orderByAsc(EmpOkrView::getLiaEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        List<EmpOkrExcel> result=new ArrayList<>();
        list.forEach(x->{
            EmpOkrExcel one=new EmpOkrExcel();
            BeanUtils.copyProperties(x,one);
            result.add(one);
        });

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

        EasyExcel.write(response.getOutputStream(), EmpOkrExcel.class)
                .sheet("导出")
                .doWrite(result);
    }


    @PostMapping("/add")
    private R add(@RequestBody JSONObject form){

        JSONArray formArray = form.getJSONArray("Form");

        List<EmpOkr> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            if(formObject.get("score").toString()==null||formObject.get("score").toString().isEmpty()){
                return R.error("评分不得为空");
            }
            if(new BigDecimal(formObject.get("score").toString()).compareTo(new BigDecimal(-20)) < 0){
                return R.error("评分不得小于-20");
            }
            if(new BigDecimal(formObject.get("score").toString()).compareTo(new BigDecimal(20)) > 0){
                return R.error("评分不得超过20");
            }

            EmpOkr one = new EmpOkr();
            one.setOkrKeyId(Long.valueOf(String.valueOf(formObject.get("okrKeyId"))));
            one.setScore(new BigDecimal(formObject.get("score").toString()));
            one.setEmpId(Long.valueOf(String.valueOf(formObject.get("empId"))));
            list.add(one);
        }
        EmpOkrService.saveBatch(list);

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

        LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId,form.getString("positionId"))
                .eq(EmployeePosition::getEmpId,form.getString("empId"));
        EmployeePosition EmployeePosition=EmployeePositionService.getOne(queryWrapper);

        String assessorId=form.getString("assessorId");
        String empId=form.getString("empId");
        LambdaQueryWrapper<TaskView> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(TaskView::getAssignee,assessorId)
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getName,"okr")
                .eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId());
        TaskView task=TaskViewMapper.selectOne(queryWrapper2);

        taskService.complete(task.getId(),map);

        return R.success();
    }


    @PostMapping("/list")
    private R<List<EmpOkrView>> list(@RequestBody JSONObject form){

        LocalDate firstDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime beginTime = LocalDateTime.of(firstDayOfLastMonth, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(lastDayOfLastMonth, LocalTime.MAX);

        List<EmpOkrView> list=EmpOkrViewService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success();
    }


    @GetMapping("/page")
    private R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){

        Page<EmpOkrView> pageInfo=new Page<EmpOkrView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpOkrView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpOkrViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @PostMapping("/getOneEmpOkr")
    private R<List<EmpOkrView>> getOneEmpOkr(@RequestBody JSONObject form){

        List<EmpOkrView> okrViews= EmpOkrViewService.lambdaQuery()
                .eq(EmpOkrView::getLiaEmpId,form.getString("empId"))
                .eq(EmpOkrView::getPositionId,form.getString("position"))
                .eq(EmpOkrView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(okrViews);
    }


    @GetMapping("/reAdd")
    private R reAdd(@RequestParam("id") String id
            ,@RequestParam("correctedValue") String correctedValue
            ,@RequestParam("empId") String empId){
        UpdateWrapper<EmpOkr> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("id",id)
                .set("corrected_value",correctedValue);
        EmpOkrService.update(updateWrapper);

        EmpOkrService.reChange(Long.valueOf(id),Long.valueOf(empId));
        return R.success();
    }


    @PostMapping("/reDelete")
    private R reAdd(@RequestBody EmpOkr one){
        one.setState(new Short("0"));
        EmpOkrService.updateById(one);

        EmpOkrService.reChange(one.getId(),one.getEmpId());
        return R.success();
    }


    @PostMapping("/reBackAdd")
    private R reBackAdd(@RequestBody JSONObject form){
        JSONArray formArray = form.getJSONArray("Form");

        List<EmpOkr> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            EmpOkr one = new EmpOkr();
            one.setOkrKeyId(Long.valueOf(String.valueOf(formObject.get("okrKeyId"))));
            one.setScore(new BigDecimal(formObject.get("score").toString()));
            one.setEmpId(Long.valueOf(String.valueOf(formObject.get("empId"))));
            list.add(one);
        }
        EmpOkrService.saveBatch(list);

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
                .and(qw -> qw.eq(BackWait::getType,"third_okr_back")
                        .or().eq(BackWait::getType,"back_second_okr"))
                .list();

        String assessorId=form.getString("assessorId");
        String empId=form.getString("empId");
        LambdaQueryWrapper<TaskView> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(TaskView::getAssignee,assessorId)
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getName,"back_okr");
        List<TaskView> task=TaskViewMapper.selectList(queryWrapper2);

        backWait.forEach(x->{
            task.forEach(y->{
                if(y.getProcInstId().equals(x.getProcessDefineId())){
                    taskService.complete(y.getId(),map);
                }
            });
        });

        return R.success();
    }


    @PostMapping("/secondBack")
    private R<List<EmpOkrView>> secondBack(@RequestBody JSONObject obj){
        List<EmpOkrView> EmpOkrViews=EmpOkrViewService.lambdaQuery()
                .eq(EmpOkrView::getPositionId,obj.getString("positionId"))
                .orderByAsc(EmpOkrView::getLiaEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(EmpOkrViews);
    }


    @GetMapping("/nowPage")
    public R<Page> nowPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize){

        Page<EmpOkrView> pageInfo=new Page<EmpOkrView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpOkrView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(EmpOkrView::getLiaEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpOkrViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/nowSearch")
    public R<Page> nowSerach(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String empId
            ,@RequestParam(defaultValue = "") String empName){

        Page<EmpOkrView> pageInfo=new Page<EmpOkrView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpOkrView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(EmpOkrView::getLiaEmpId,empId)
                .like(EmpOkrView::getName,empName)
                .orderByAsc(EmpOkrView::getLiaEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpOkrViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/pastPage")
    public R<Page> pastPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<EmpOkrView> pageInfo=new Page<EmpOkrView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpOkrView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(EmpOkrView::getLiaEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        EmpOkrViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/pastSearch")
    public R<Page> pastPage(@RequestParam("page") String page
            ,@RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String empId
            ,@RequestParam(defaultValue = "") String empName
            ,@RequestParam(defaultValue = "") String beginTime
            ,@RequestParam(defaultValue = "") String endTime){

        Page<EmpOkrView> pageInfo=new Page<EmpOkrView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpOkrView> queryWrapper=new LambdaQueryWrapper<>();
        if(beginTime.equals("")){
            LocalDate today = LocalDate.now();
            LocalDateTime beginDay = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

            queryWrapper.like(EmpOkrView::getLiaEmpId,empId)
                    .like(EmpOkrView::getName,empName)
                    .orderByAsc(EmpOkrView::getLiaEmpId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        }
        else {
            LocalDateTime beginDay = LocalDateTime.of(LocalDate.parse(beginTime), LocalTime.MIN);
            LocalDateTime endDay = LocalDateTime.of(LocalDate.parse(endTime), LocalTime.MAX);

            queryWrapper.like(EmpOkrView::getLiaEmpId,empId)
                    .like(EmpOkrView::getName,empName)
                    .orderByAsc(EmpOkrView::getLiaEmpId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay)
                    .apply(StringUtils.checkValNotNull(endDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endDay);
        }
        EmpOkrViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }
}
