package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.OkrForm;
import com.example.workflow.entity.OkrKey;
import com.example.workflow.entity.OkrRule;
import com.example.workflow.entity.OkrView;
import com.example.workflow.mapper.OkrViewMapper;
import com.example.workflow.service.OkrKeyService;
import com.example.workflow.service.OkrRuleService;
import com.example.workflow.service.OkrViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/OkrRule")
public class OkrRuleController {
    @Autowired
    private OkrViewMapper OkrViewMapper;
    @Autowired
    private OkrRuleService OkrRuleService;
    @Autowired
    private OkrKeyService OkrKeyService;
    @Autowired
    private OkrViewService OkrViewService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @PostMapping("/list")
    private R<List<OkrView>> list(@RequestBody JSONObject obj){

        LambdaQueryWrapper<OkrView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(OkrView::getAssessorId,obj.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<OkrView> list=OkrViewMapper.selectList(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/add")
    private R add(@RequestBody OkrForm form){

        OkrRule one=OkrRuleService.split(form);
        OkrRuleService.save(one);

        List<OkrKey> list=form.getKeyList();
        list.forEach(x->{
            x.setRuleId(one.getId());
        });
        OkrKeyService.saveBatch(list);
        return R.success();
    }


    @PostMapping("/getKey")
    private R<List<OkrKey>> getKeyList(@RequestBody JSONObject obj){
        List<OkrKey> list=OkrKeyService.lambdaQuery().eq(OkrKey::getRuleId,obj.getString("ruleId")).list();

        return R.success(list);
    }


    @PostMapping("/update")
    private R update(@RequestBody OkrForm form){
        LambdaQueryWrapper<OkrKey> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(OkrKey::getRuleId,form.getId());
        OkrKeyService.remove(queryWrapper);

        OkrRuleService.removeById(form.getId());

        OkrRule one=OkrRuleService.split(form);
        OkrRuleService.save(one);

        List<OkrKey> list=form.getKeyList();
        list.forEach(x->{
            x.setRuleId(one.getId());
        });
        OkrKeyService.saveBatch(list);
        return R.success();
    }

    @PostMapping("/delete")
    private R delete(@RequestBody JSONObject obj){
        LambdaQueryWrapper<OkrKey> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(OkrKey::getRuleId,obj.getString("id"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        OkrKeyService.remove(queryWrapper);

        OkrRuleService.removeById(obj.getString("id"));

        return R.success();
    }


    @PostMapping("/pastList")
    private R<List<OkrView>> pastList(@RequestBody JSONObject obj){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        LambdaQueryWrapper<OkrView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(OkrView::getAssessorId,obj.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        List<OkrView> list=OkrViewMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/getEmpPositionOkr")
    private R<List<OkrView>> getEmpPositionOkr(@RequestBody JSONObject obj){
        LocalDate lastToday = LocalDate.now().minusMonths(1);
        LocalDateTime lastBeginTime = LocalDateTime.of(lastToday.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime lastEndTime = LocalDateTime.of(lastToday.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

        List<OkrView> list= OkrViewService.lambdaQuery()
                .eq(OkrView::getPositionId,obj.getString("positionId"))
                .eq(OkrView::getLiaEmpId,obj.getString("empId"))
                .eq(OkrView::getAssessorId,obj.getString("assessorId"))
                .apply(StringUtils.checkValNotNull(lastBeginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", lastBeginTime)
                .apply(StringUtils.checkValNotNull(lastEndTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", lastEndTime)
                .list();

        return R.success(list);
    }





}
