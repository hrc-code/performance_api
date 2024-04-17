package com.example.workflow.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.EmpCheckState;
import com.example.workflow.entity.EmpCheckStateView;
import com.example.workflow.service.EmpCheckStateService;
import com.example.workflow.service.EmpCheckStateViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@RestController
@RequestMapping("/EmpCheckState")
public class EmpCheckStateController {
    @Autowired
    private EmpCheckStateService EmpCheckStateService;
    @Autowired
    private EmpCheckStateViewService EmpCheckStateViewService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @GetMapping("/nowPage")
    public R<Page> pastPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize){

        Page<EmpCheckStateView> pageInfo=new Page<EmpCheckStateView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpCheckStateView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(EmpCheckStateView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpCheckStateViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/point")
    private R point(@RequestBody JSONObject obj){
        LambdaQueryWrapper<EmpCheckState> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpCheckState::getEmpId,obj.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpCheckState state=EmpCheckStateService.getOne(queryWrapper);
        if(state!=null){
            state.setCheckState(Short.parseShort("1"));
            state.setCheckCount(state.getCheckCount()+1);
            EmpCheckStateService.save(state);
        }
        else{
            EmpCheckState newOne=new EmpCheckState();
            newOne.setEmpId(Long.valueOf(obj.getString("empId")));
            newOne.setCheckState(Short.parseShort("1"));
            newOne.setCheckCount(1);
            EmpCheckStateService.save(newOne);
        }

        return R.success();
    }


    @PostMapping("/match")
    private R<Boolean> match(@RequestBody JSONObject obj){
        LambdaQueryWrapper<EmpCheckState> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpCheckState::getEmpId,obj.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpCheckState state=EmpCheckStateService.getOne(queryWrapper);
        if(state!=null) {
            return R.success(true);
        }
        else{
            return R.success(false);
        }
    }
}
