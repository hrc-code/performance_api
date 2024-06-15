package com.example.workflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.model.entity.EmpReward;
import com.example.workflow.model.entity.EmpRewardView;
import com.example.workflow.service.EmpRewardService;
import com.example.workflow.service.EmpRewardViewService;
import com.example.workflow.service.EmployeeCoefficientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/EmpReward")
public class EmpRewardController {
    @Autowired
            private EmpRewardService EmpRewardService;
    @Autowired
            private EmpRewardViewService EmpRewardViewService;
    @Autowired
            private EmployeeCoefficientService EmployeeCoefficientService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @PostMapping("/add")
    private R add(@RequestBody List<EmpReward> form){
        EmpRewardService.saveBatch(form);

        EmployeeCoefficientService.fileOne(form.get(0).getEmpId(),form.get(0).getPositionId());
        return R.success();
    }


    @GetMapping("/page")
    private R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        Page<EmpRewardView> pageInfo=new Page<EmpRewardView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpRewardView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpRewardViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/delete")
    private R delete(@RequestBody EmpReward one){
        System.out.println(one);
        EmpRewardService.removeById(one);

        EmployeeCoefficientService.fileOne(one.getEmpId(),one.getPositionId());
        return R.success();
    }

    @PostMapping("/updateOne")
    private R updateOne(@RequestBody EmpReward one){
        EmpRewardService.updateById(one);

        EmployeeCoefficientService.fileOne(one.getEmpId(),one.getPositionId());
        return R.success();
    }

}
