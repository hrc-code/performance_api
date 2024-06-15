package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.listener.RegionExcelReadListener;
import com.example.workflow.mapper.EmployeeCoefficientMapper;
import com.example.workflow.mapper.RegionCoefficientMapper;
import com.example.workflow.model.entity.EmpCoefficient;
import com.example.workflow.model.entity.RegionCoefficient;
import com.example.workflow.model.feedback.ErrorExcelWrite;
import com.example.workflow.model.feedback.RegionError;
import com.example.workflow.model.pojo.RegionExcel;
import com.example.workflow.service.EmployeeCoefficientService;
import com.example.workflow.service.RegionCoefficientService;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/RegionCoefficient")
public class RegionCoefficientController {
    @Autowired
    private RegionCoefficientMapper RegionCoefficientMapper;
    @Autowired
    private RegionCoefficientService RegionCoefficientService;
    @Autowired
    private EmployeeCoefficientService EmployeeCoefficientService;
    @Autowired
    private EmployeeCoefficientMapper EmployeeCoefficientMapper;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @PostMapping("/list")
    private R<List<RegionCoefficient>> list(){
        LambdaQueryWrapper<RegionCoefficient> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(RegionCoefficient::getId)
                .eq(RegionCoefficient::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<RegionCoefficient> list=RegionCoefficientMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @GetMapping("/regionNowPage")
    public R<Page> regionNowPage(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        Page<RegionCoefficient> pageInfo=new Page<RegionCoefficient>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<RegionCoefficient> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        RegionCoefficientService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/add")
    private R add(@RequestBody RegionCoefficient form){
        if(form.getRegion()==null)
            return R.error("地区不得为空");
        if(form.getCoefficient()==null)
            return R.error("地区系数不得为空");

        RegionCoefficientMapper.insert(form);
        return R.success();
    }

    @PostMapping("/match")
    private R matchCoefficient(){
        List<RegionCoefficient> list=RegionCoefficientMapper.selectList(null);

        String rules=RegionCoefficientService.defineRule(list);

        LambdaQueryWrapper<EmpCoefficient> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpCoefficient::getState,1);
        List<EmpCoefficient> resultList=EmployeeCoefficientMapper.selectList(queryWrapper);

        KieHelper helper = new KieHelper();
        helper.addContent(rules, ResourceType.DRL);
        KieSession kSession = helper.build().newKieSession();

        return R.success();
    }

    @PostMapping("/delete")
    private R delete(@RequestBody RegionCoefficient one){
        RegionCoefficientMapper.deleteById(one);
        return R.success();
    }
    @PostMapping("/update")
    private R update(@RequestBody RegionCoefficient form){
        if(form.getRegion()==null)
            return R.error("地区不得为空");
        if(form.getCoefficient()==null)
            return R.error("地区系数不得为空");

        RegionCoefficientMapper.updateById(form);
        return R.success();
    }


    @PostMapping("/copy")
    private R copy(){
        List<RegionCoefficient> list=RegionCoefficientService.lambdaQuery()
                .eq(RegionCoefficient::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(!list.isEmpty()){
            return R.error("已复制上月地区系数,请勿重复操作");
        }

        RegionCoefficientService.monthCopy();
        return R.success();
    }

    @GetMapping("/regionPastPage")
    public R<Page> regionPastPage(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<RegionCoefficient> pageInfo=new Page<RegionCoefficient>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<RegionCoefficient> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        RegionCoefficientService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @GetMapping("/pastSearch")
    public R<Page> pastPage(@RequestParam("page") String page
            ,@RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String region
            ,@RequestParam(defaultValue = "") String beginTime
            ,@RequestParam(defaultValue = "") String endTime){

        Page<RegionCoefficient> pageInfo=new Page<RegionCoefficient>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<RegionCoefficient> queryWrapper=new LambdaQueryWrapper<>();

        if(beginTime.isEmpty()){
            LocalDate today = LocalDate.now();
            LocalDateTime beginDay = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

            queryWrapper.like(RegionCoefficient::getRegion,region)
                    .orderByAsc(RegionCoefficient::getId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay);
        }
        else {
            LocalDateTime beginDay = LocalDateTime.of(LocalDate.parse(beginTime), LocalTime.MIN);
            LocalDateTime endDay = LocalDateTime.of(LocalDate.parse(endTime), LocalTime.MAX);

            queryWrapper.like(RegionCoefficient::getRegion,region)
                    .orderByAsc(RegionCoefficient::getId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay)
                    .apply(StringUtils.checkValNotNull(endDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endDay);
        }
        RegionCoefficientService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/upload")
    public R uploadExcel(MultipartFile file, HttpServletResponse response) throws IOException {
        EasyExcel.read(file.getInputStream(), RegionExcel.class, new RegionExcelReadListener()).sheet().doRead();
        if(!ErrorExcelWrite.getErrorCollection().isEmpty()){
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

            EasyExcel.write(response.getOutputStream(), RegionError.class).sheet("错误部分").doWrite(ErrorExcelWrite.getErrorCollection());
        }
        ErrorExcelWrite.clearErrorCollection();
        return R.success();
    }
}
