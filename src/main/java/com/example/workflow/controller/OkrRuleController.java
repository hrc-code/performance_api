package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.entity.*;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.feedback.OkrError;
import com.example.workflow.feedback.ScoreError;
import com.example.workflow.listener.OkrExcelReadListener;
import com.example.workflow.listener.ScoreExcelReadListener;
import com.example.workflow.mapper.OkrViewMapper;
import com.example.workflow.pojo.OkrExcel;
import com.example.workflow.pojo.ScoreExcel;
import com.example.workflow.service.OkrKeyService;
import com.example.workflow.service.OkrRuleService;
import com.example.workflow.service.OkrViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
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


    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        LocalDateTime lastBeginTime = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
        LocalDateTime latsEndTime = LocalDateTime.of(today.withDayOfMonth(1).minusDays(1), LocalTime.MAX);

        Page<OkrView> pageInfo=new Page<OkrView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<OkrView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(lastBeginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", lastBeginTime)
                .apply(StringUtils.checkValNotNull(latsEndTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", latsEndTime);
        OkrViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

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
        one.setCreateTime(LocalDateTime.now());
        one.setUpdateTime(LocalDateTime.now());
        OkrRuleService.save(one);

        List<OkrKey> list=form.getKeyList();
        list.forEach(x->{
            x.setRuleId(one.getId());
            x.setCreateTime(LocalDateTime.now());
            x.setUpdateTime(LocalDateTime.now());
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
        one.setCreateTime(LocalDateTime.now());
        one.setUpdateTime(LocalDateTime.now());
        OkrRuleService.save(one);

        List<OkrKey> list=form.getKeyList();
        list.forEach(x->{
            x.setRuleId(one.getId());
            x.setCreateTime(LocalDateTime.now());
            x.setUpdateTime(LocalDateTime.now());
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

        System.out.println(obj.getString("id"));
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
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDateTime lastBeginTime = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime lastEndTime = lastMonth.atEndOfMonth().atTime(LocalTime.MAX);
        /*LocalDate lastToday = LocalDate.now().minusMonths(1);
        LocalDateTime lastBeginTime = LocalDateTime.of(lastToday.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime lastEndTime = LocalDateTime.of(lastToday.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);*/

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

    @PostMapping("/upload")
    public void uploadExcel(MultipartFile file, HttpServletResponse response) throws IOException {
        EasyExcel.read(file.getInputStream(), OkrExcel.class, new OkrExcelReadListener()).sheet().doRead();

        if(!ErrorExcelWrite.getErrorCollection().isEmpty()){
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

            EasyExcel.write(response.getOutputStream(), OkrError.class).sheet("错误部分").doWrite(ErrorExcelWrite.getErrorCollection());
        }
        ErrorExcelWrite.clearErrorCollection();
    }

}
