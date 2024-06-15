package com.example.workflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.model.vo.HistoryTask;
import com.example.workflow.service.HistoryTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@RestController
@RequestMapping("/HistoryTask")
public class HistoryTaskController {

    @Autowired
    private HistoryTaskService HistoryTaskService;
    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") String page,
                        @RequestParam("page_size") String pageSize,
                        @RequestParam("type") String type,
                        @RequestParam("assessorId") String assessorId){

        Page<HistoryTask> pageInfo=new Page<HistoryTask>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<HistoryTask> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(HistoryTask::getAssessorId,assessorId)
                .eq(HistoryTask::getName,type)
                .eq(HistoryTask::getDeleteReason,"completed")
                .isNotNull(HistoryTask::getEmpName)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (start_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (start_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        HistoryTaskService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }
}
