package com.example.workflow.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.dto.EmpCoeForm;
import com.example.workflow.entity.*;
import com.example.workflow.mapper.CoefficientViewMapper;
import com.example.workflow.mapper.EmpKpiViewMapper;
import com.example.workflow.mapper.EmpOkrViewMapper;
import com.example.workflow.mapper.EmpPieceViewMapper;
import com.example.workflow.mapper.EmpRewardViewMapper;
import com.example.workflow.mapper.EmpScoreViewMapper;
import com.example.workflow.mapper.ResultKpiEmpViewMapper;
import com.example.workflow.mapper.ResultOkrViewMapper;
import com.example.workflow.mapper.ResultPieceEmpViewMapper;
import com.example.workflow.mapper.ResultScoreEmpViewMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.service.*;
import com.example.workflow.vo.PositionAssessorView;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequestMapping("/EmpCoefficient")
public class EmpCoefficientController {
    @Autowired
    private CoefficientViewService CoefficientViewService;
    @Autowired
    private EmpCoefficientService EmpCoefficientService;
    @Autowired
    private ResultScoreEmpViewMapper ResultScoreEmpViewMapper;
    @Autowired
    private ResultPieceEmpViewMapper ResultPieceEmpViewMapper;
    @Autowired
    private ResultKpiEmpViewMapper ResultKpiEmpViewMapper;
    @Autowired
    private CoefficientViewMapper CoefficientViewMapper;
    @Autowired
    private EmpWageService EmpWageService;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private ResultOkrViewMapper ResultOkrViewMapper;
    @Autowired
    private EmpScoreViewMapper EmpScoreViewMapper;
    @Autowired
    private EmpPieceViewMapper EmpPieceViewMapper;
    @Autowired
    private EmpKpiViewMapper EmpKpiViewMapper;
    @Autowired
    private EmpOkrViewMapper EmpOkrViewMapper;
    @Autowired
            private EmpRewardViewMapper EmpRewardViewMapper;
    @Autowired
            private EmployeeCoefficientService EmployeeCoefficientService;
    @Autowired
            private RegionCoefficientService RegionCoefficientService;
    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @GetMapping("/page")
    private R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){

        Page<CoefficientView> pageInfo=new Page<CoefficientView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<CoefficientView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .orderByDesc(CoefficientView::getEmpId);
        CoefficientViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/copy")
    private R copy(){
        List<EmpCoefficient> list= EmpCoefficientService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(!list.isEmpty())
            return R.error("本月员工系数已复制，请勿重复操作");

        List<RegionCoefficient> regionCoefficientList=RegionCoefficientService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        if(regionCoefficientList.isEmpty())
            return R.error("未进行本月地域系数配置，请前往“地域系数”进行配置或复制上一个月");

        EmpCoefficientService.monthCopy();

        return R.success();
    }

    @GetMapping("/search")
    public R<Page> searchPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String empName){
        Page<CoefficientView> pageInfo=new Page<CoefficientView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<CoefficientView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CoefficientView::getEmpId)
                .like(CoefficientView::getEmpName,empName)
                .eq(CoefficientView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        CoefficientViewService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    @GetMapping("/coefficientPastPage")
    public R<Page> coefficientPastPage(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<CoefficientView> pageInfo=new Page<CoefficientView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<CoefficientView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        CoefficientViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @PostMapping("/update")
    private R<EmpCoefficient> update(@RequestBody EmpCoefficient one){
        EmpCoefficientService.updateById(one);

        return R.success();
    }

    @PostMapping("/file")
    private R file(@RequestBody JSONObject obj){
        Long empId=Long.valueOf(obj.getString("empId"));
        Long positionId=Long.valueOf(obj.getString("positionId"));

        EmployeeCoefficientService.fileOne(empId,positionId);

        LambdaQueryWrapper<EmployeePosition> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(EmployeePosition::getEmpId,empId)
                .eq(EmployeePosition::getPositionId,positionId)
                .eq(EmployeePosition::getState,1);
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper5);

        LambdaQueryWrapper<TaskView> queryWrapper7=new LambdaQueryWrapper<>();
        queryWrapper7.eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId())
                .eq(TaskView::getStartUserId,empId)
                .eq(TaskView::getAssignee,obj.getString("assessorId"));
        TaskView task= TaskViewMapper.selectOne(queryWrapper7);
        taskService.complete(task.getId());

        UpdateWrapper<EmployeePosition> updateWrapper =new UpdateWrapper<>();
        updateWrapper
                .set("process_definition_id","")
                .eq("emp_id",empId)
                .eq("position_id",positionId)
                .eq("state",1);
        EmployeePositionService.update(updateWrapper);

        return R.success();
    }

    @PostMapping("/fileAll")
    private R fileAll(@RequestBody JSONObject form){
        JSONArray formArray = form.getJSONArray("Form");

        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            Long empId=Long.valueOf(String.valueOf(formObject.get("empId")));
            Long positionId=Long.valueOf(String.valueOf(formObject.get("positionId")));
            EmployeeCoefficientService.fileOne(empId,positionId);

            LambdaQueryWrapper<EmployeePosition> queryWrapper5=new LambdaQueryWrapper<>();
            queryWrapper5.eq(EmployeePosition::getEmpId,empId)
                    .eq(EmployeePosition::getPositionId,positionId)
                    .eq(EmployeePosition::getState,1);
            EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper5);

            LambdaQueryWrapper<TaskView> queryWrapper7=new LambdaQueryWrapper<>();
            queryWrapper7.eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId())
                    .eq(TaskView::getStartUserId,empId)
                    .eq(TaskView::getAssignee,form.getString("assessorId"));
            TaskView task= TaskViewMapper.selectOne(queryWrapper7);
            taskService.complete(task.getId());

            UpdateWrapper<EmployeePosition> updateWrapper =new UpdateWrapper<>();
            updateWrapper
                    .set("process_definition_id","")
                    .eq("emp_id",empId)
                    .eq("position_id",positionId)
                    .eq("state",1);
            EmployeePositionService.update(updateWrapper);
        }

        return R.success();
    }

    @PostMapping("/changeAll")
    private R changeAll(@RequestBody EmpCoeForm empCoeForm){
        if(empCoeForm.getEmpList().isEmpty())
            return R.error("员工选择不得为空");

        if(empCoeForm.getOption()==null)
            return R.error("未选择批量修改内容");
        else if (empCoeForm.getOption().equals(1)) {
            if(empCoeForm.getBaseWage()==null)
                return R.error("基础工资不得为空");
        }
        else if(empCoeForm.getOption().equals(4)){
            if(empCoeForm.getPerformanceWage()==null)
                return R.error("绩效工资不得为空");
        }

        empCoeForm.getEmpList().forEach(x->{
            EmpCoefficient empCoefficient=new EmpCoefficient();
            BeanUtils.copyProperties(x, empCoefficient);
            if(empCoeForm.getOption().equals(1))
                empCoefficient.setWage(empCoeForm.getBaseWage());
            else if(empCoeForm.getOption().equals(4))
                empCoefficient.setPerformanceWage(empCoeForm.getPerformanceWage());
            EmpCoefficientService.updateById(empCoefficient);
        });

        return R.success();
    }

}
