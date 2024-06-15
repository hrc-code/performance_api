package com.example.workflow.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.mapper.EmpOkrViewMapper;
import com.example.workflow.model.entity.EmpOkrView;
import com.example.workflow.model.entity.ResultOkr;
import com.example.workflow.service.ResultOkrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ResultOkr")
public class ResultOkrController {
    @Autowired
    private EmpOkrViewMapper EmpOkrViewMapper;
    @Autowired
    private ResultOkrService ResultOkrService;

    @PostMapping("/list")
    private R<List<EmpOkrView>> list(@RequestBody JSONObject obj){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

        LambdaQueryWrapper<EmpOkrView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpOkrView::getLiaEmpId,obj.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpOkrView> list=EmpOkrViewMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/add")
    private R add(@RequestBody JSONObject form){
        JSONArray formArray = form.getJSONArray("Form");

        List<ResultOkr> list = new ArrayList<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formObject = formArray.getJSONObject(i);

            ResultOkr one = new ResultOkr();
            one.setEmpOkrId(Long.valueOf(String.valueOf(formObject.get("empOkrId"))));
            one.setExamine(new Short(formObject.get("examine").toString()));
            one.setAssessorId(Long.valueOf(String.valueOf(formObject.get("assessorId"))));
            list.add(one);
        }
        ResultOkrService.saveBatch(list);

        return R.success();
    }
}
