package com.example.workflow.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.entity.*;
import com.example.workflow.mapper.EmpPositionViewMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.service.*;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/EmployeePosition")
public class EmployeePositionController {
    @Autowired
    private EmpPositionViewMapper EmpPositionViewMapper;
    @Autowired
    private EmpPositionViewService EmpPositionViewService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private PositionService PositionService;
    @Autowired
            private EmpScoreService EmpScoreService;
    @Autowired
            private EmpPieceService EmpPieceService;
    @Autowired
            private EmpKpiService EmpKpiService;
    @Autowired
            private EmpOkrService EmpOkrService;
    @Autowired
            private EmpRewardService EmpRewardService;


    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @PostMapping("/list")
    private R<List<TaskInf>> list(@RequestBody JSONObject obj){
        String data=obj.toJSONString();
        JSONObject json = JSON.parseObject(data);

        LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpPositionView::getPositionId,json.getString("positionId"));
        List<EmpPositionView> empList=EmpPositionViewMapper.selectList(queryWrapper);

        List<TaskInf> result=new ArrayList<>();
        empList.forEach(x->{
            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,String.valueOf(x.getEmpId()));
            TaskView task=TaskViewMapper.selectOne(Wrapper);

            if(task!=null) {
                TaskInf one = new TaskInf();
                one.setId(task.getId());
                one.setAssignee(task.getAssignee());
                one.setName(task.getName());
                one.setProcInstId(task.getProcInstId());
                one.setStartUserId(task.getStartUserId());
                one.setState(task.getState());
                one.setSuspensionState(task.getSuspensionState());
                one.setPosition(x.getPosition());
                one.setPositionId(x.getPositionId());
                one.setEmpId(x.getEmpId());
                one.setEmpName(x.getEmpName());
                result.add(one);
            }
        });

        return R.success(result);
    }


    @GetMapping("/page")
    private R<Page<EmpPositionView>> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        Page<EmpPositionView> pageInfo=new Page<EmpPositionView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(EmpPositionView::getEmpId)
                .eq(EmpPositionView::getState,1);

        EmpPositionViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/add")
    private R add(@RequestBody JSONObject form){

        LambdaQueryWrapper<EmployeePosition> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getEmpId,form.getString("empId"));
        if(!EmployeePositionService.list(queryWrapper2).isEmpty()){
            return R.error("该员工已任职岗位，请勿重复操作");
        }

        JSONArray formArray = form.getJSONArray("positionList");
        List<String> positionList = new ArrayList<>();

        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);
            positionList.add(formObject.getString("positionId"));
        }
        Set<String> set = new HashSet<>(positionList);
        if (set.size() < positionList.size()) {
            return R.error("员工不可重复任职同一岗位");
        }

        LambdaQueryWrapper<EmployeePosition> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getEmpId,form.getString("empId"));
        EmployeePositionService.remove(queryWrapper);

        List<EmployeePosition> employeePositionList=new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            EmployeePosition x=new EmployeePosition();
            x.setEmpId(Long.valueOf(form.getString("empId")));
            x.setPositionId(Long.valueOf(formObject.getString("positionId")));
            String valueAsString = formObject.getString("posiPercent");
            BigDecimal valueAsBigDecimal = new BigDecimal(valueAsString);
            x.setPosiPercent(valueAsBigDecimal);

            LambdaQueryWrapper<Position> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(Position::getId,Long.valueOf(formObject.getString("positionId")));
            Short type= PositionService.getOne(Wrapper).getType();

            if(type==5)
                x.setProcessKey("Process_1gzouwy");
            else if(type==4)
                x.setProcessKey("Process_1whe0gq");
            else if(type==3)
                x.setProcessKey("Process_01p7ac7");
            employeePositionList.add(x);
        }
        EmployeePositionService.saveBatch(employeePositionList);

        return R.success();
    }


    @PostMapping("/getEmpPositionList")
    private R<List<EmpPositionView>> getEmpPositionList(@RequestBody JSONObject obj){

        LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpPositionView::getEmpId,obj.getString("empId"));
        List<EmpPositionView> list=EmpPositionViewMapper.selectList(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/update")
    private R update(@RequestBody JSONObject form){
        JSONArray formArray = form.getJSONArray("positionList");
        /*List<String> positionList = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);
            positionList.add(formObject.getString("positionId"));
        }
        Set<String> set = new HashSet<>(positionList);
        if (set.size() < positionList.size()) {
            return R.error("员工不可重复任职同一岗位");
        }*/

        LambdaQueryWrapper<EmployeePosition> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getEmpId,form.getString("empId"));
        EmployeePositionService.remove(queryWrapper);

        List<EmployeePosition> employeePositionList=new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            EmployeePosition x=new EmployeePosition();
            x.setEmpId(Long.valueOf(form.getString("empId")));

            if(formObject.getString("positionId").isEmpty())
                return R.error("请选择人员岗位");
            x.setPositionId(Long.valueOf(formObject.getString("positionId")));

            if(formObject.getString("posiPercent").isEmpty())
                return R.error("请填写岗位比例");
            String valueAsString = formObject.getString("posiPercent");
            BigDecimal valueAsBigDecimal = new BigDecimal(valueAsString);
            x.setPosiPercent(valueAsBigDecimal);

            LambdaQueryWrapper<Position> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(Position::getId,formObject.getString("positionId"));
            Short type= PositionService.getOne(Wrapper).getType();

            if(type==5)
                x.setProcessKey("Process_1gzouwy");
            else if(type==4)
                x.setProcessKey("Process_1whe0gq");
            else if(type==3)
                x.setProcessKey("Process_01p7ac7");
            employeePositionList.add(x);
        }
        EmployeePositionService.saveBatch(employeePositionList);

        return R.success();
    }


    @PostMapping("/delete")
    private R remove(@RequestBody EmpPositionView form){
        LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getEmpId,form.getEmpId());
        EmployeePositionService.remove(queryWrapper);

        LambdaQueryWrapper<EmpScore> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(EmpScore::getEmpId,form.getEmpId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        EmpScoreService.remove(queryWrapper1);

        LambdaQueryWrapper<EmpPiece> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmpPiece::getEmpId,form.getEmpId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        EmpPieceService.remove(queryWrapper2);

        LambdaQueryWrapper<EmpKpi> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(EmpKpi::getEmpId,form.getEmpId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        EmpKpiService.remove(queryWrapper3);

        LambdaQueryWrapper<EmpOkr> queryWrapper4=new LambdaQueryWrapper<>();
        queryWrapper4.eq(EmpOkr::getEmpId,form.getEmpId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        EmpOkrService.remove(queryWrapper4);

        LambdaQueryWrapper<EmpReward> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(EmpReward::getEmpId,form.getEmpId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        EmpRewardService.remove(queryWrapper5);

        return R.success();
    }

    @GetMapping("/search")
    public R<Page> searchPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String empName){
        Page<EmpPositionView> pageInfo=new Page<EmpPositionView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(EmpPositionView::getEmpId)
                .like(EmpPositionView::getEmpName,empName)
                .eq(EmpPositionView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpPositionViewService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

}
