package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.entity.*;
import com.example.workflow.listener.KpiExcelReadListener;
import com.example.workflow.listener.PieceExcelReadListener;
import com.example.workflow.listener.PositionKpiExcelReadListener;
import com.example.workflow.listener.PositionPieceExcelReadListener;
import com.example.workflow.mapper.KpiRuleMapper;
import com.example.workflow.mapper.PositionKpiMapper;
import com.example.workflow.mapper.PositionKpiViewMapper;
import com.example.workflow.pojo.KpiExcel;
import com.example.workflow.pojo.PieceExcel;
import com.example.workflow.pojo.PositionKpiExcel;
import com.example.workflow.pojo.PositionPieceExcel;
import com.example.workflow.service.KpiPercentService;
import com.example.workflow.service.KpiRulePercentService;
import com.example.workflow.service.KpiRuleService;
import com.example.workflow.service.PositionKpiSerivce;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/KpiRule")
public class KpiRuleController {
    @Autowired
    private KpiRuleMapper KpiRuleMapper;
    @Autowired
    private KpiRuleService KpiRuleService;
    @Autowired
    private KpiPercentService KpiPercentService;
    @Autowired
    private KpiRulePercentService KpiRulePercentService;
    @Autowired
    private PositionKpiMapper PositionKpiMapper;
    @Autowired
    private PositionKpiViewMapper PositionKpiViewMapper;
    @Autowired
    private PositionKpiSerivce PositionKpiSerivce;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        Page<KpiRulePercent> pageInfo=new Page<KpiRulePercent>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<KpiRulePercent> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .orderByAsc(KpiRulePercent::getName)
                .orderByAsc(KpiRulePercent::getKpiKey)
                .eq(KpiRulePercent::getState,1);
        KpiRulePercentService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @PostMapping("/update")
    private R update(@RequestBody KpiRuleForm form){
        if(form.getName()==null)
            return R.error("条目名称不得为空");
        else if (form.getTarget1()==null)
            return R.error("条目一不得为空");
        else if(form.getTarget2()==null)
            return R.error("条目二不得为空");
        else if(form.getPercentList()==null)
            return R.error("规则不得为空");

        KpiRule kpiRule=KpiRuleService.splitForm(form);
        KpiRuleMapper.updateById(kpiRule);

        LambdaQueryWrapper<KpiPercent> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(KpiPercent::getKpiId,kpiRule.getId())
                .eq(KpiPercent::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;;
        KpiPercentService.remove(queryWrapper);

        List<KpiPercent> list=KpiPercentService.splitForm(form,form.getId());
        KpiPercentService.saveBatch(list);

        return R.success();
    }

    @PostMapping("/delete")
    private R delete(@RequestBody KpiRule one){

        LambdaQueryWrapper<KpiPercent> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(KpiPercent::getKpiId,one.getId());
        KpiPercentService.remove(queryWrapper);

        KpiRuleMapper.deleteById(one);

        return R.success();
    }

    @PostMapping("/getPositionKpi")
    private R<List<PositionKpiView>> list(@RequestBody JSONObject obj){

        LambdaQueryWrapper<PositionKpiView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionKpiView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<PositionKpiView> list=PositionKpiViewMapper.selectList(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/add")
    private R add(@RequestBody KpiRuleForm form){
        if(form.getName()==null)
            return R.error("条目名称不得为空");
        else if (form.getTarget1()==null)
            return R.error("条目一不得为空");
        else if(form.getTarget2()==null)
            return R.error("条目二不得为空");
        else if(form.getPercentList()==null)
            return R.error("规则不得为空");

        KpiRule kpiRule=KpiRuleService.splitForm(form);
        KpiRuleMapper.insert(kpiRule);

        List<KpiPercent> list=KpiPercentService.splitForm(form,kpiRule.getId());
        KpiPercentService.saveBatch(list);
        return R.success();
    }

    /**
     * 获取当条kpi的百分比分成，用于修改（time）
     * @param kpiId
     * @return
     */
    @GetMapping("/getKpiPercent")
    private R<List<KpiPercent>> getKpiPercent(@RequestParam("kpiId") String kpiId){
        LambdaQueryWrapper<KpiPercent> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(KpiPercent::getKpiId,kpiId)
                .orderByAsc(KpiPercent::getKpiId)
                .orderByAsc(KpiPercent::getKpiKey)
                .eq(KpiPercent::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;

        List<KpiPercent> list=KpiPercentService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 无条件获取所有kpi项目（time）
     * @return
     */
    @PostMapping("/getRuleList")
    private R<List<KpiRule>> getRulelist(){

        LambdaQueryWrapper<KpiRule> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(KpiRule::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<KpiRule> list=KpiRuleService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 添加kpi项目（time）
     * @param form
     * @return
     */
    @PostMapping("/addAssessor")
    private R addAssessor(@RequestBody PositionKpi form){
        LambdaQueryWrapper<PositionKpi> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionKpi::getPositionId,form.getPositionId())
                .eq(PositionKpi::getKpiId,form.getKpiId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;

        List<PositionKpi> positionKpis=PositionKpiSerivce.list(queryWrapper);
        if(positionKpis!=null&&!positionKpis.isEmpty()){
            return R.error("kpi条目已存在，不可重复添加");
        }

        PositionKpiMapper.insert(form);
        return  R.success();
    }

    /**
     * 获取该岗位的kpi项目（time）
     * @param obj positionId
     * @return
     */
    @PostMapping("/getPositionKpiList")
    private R<List<PositionKpiView>> getPositionKpiList(@RequestBody JSONObject obj){

        LambdaQueryWrapper<PositionKpiView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionKpiView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<PositionKpiView> list=PositionKpiViewMapper.selectList(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/updateAssessor")
    private R updateAssessor(@RequestBody PositionKpi form){
        LambdaQueryWrapper<PositionKpi> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionKpi::getPositionId,form.getPositionId())
                .eq(PositionKpi::getKpiId,form.getKpiId())
                .eq(PositionKpi::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<PositionKpi> positionKpis=PositionKpiSerivce.list(queryWrapper);
        if(positionKpis.size()>1){
            return R.error("kpi条目重复，不可修改");
        }

        PositionKpiMapper.updateById(form);
        return  R.success();
    }


    @PostMapping("/removeAssessor")
    private R removeAssessor(@RequestBody PositionKpiView form){
        LambdaQueryWrapper<PositionKpi> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionKpi::getPositionId,form.getPositionId())
                .eq(PositionKpi::getKpiId,form.getKpiId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        PositionKpiSerivce.remove(queryWrapper);

        return R.success();
    }

    @PostMapping("/copy")
    private R copy(){
        List<KpiRule> list= KpiRuleService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(!list.isEmpty())
            return R.error("本月kpi考核条目已复制，请勿重复操作");

        KpiRuleService.monthCopy();

        return R.success();
    }

    @GetMapping("/kpiPastPage")
    public R<Page> kpiPastPage(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<KpiRulePercent> pageInfo=new Page<KpiRulePercent>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<KpiRulePercent> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        KpiRulePercentService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @GetMapping("/pastSearch")
    public R<Page> pastPage(@RequestParam("page") String page
            ,@RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String name
            ,@RequestParam(defaultValue = "") String beginTime
            ,@RequestParam(defaultValue = "") String endTime){

        Page<KpiRulePercent> pageInfo=new Page<KpiRulePercent>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<KpiRulePercent> queryWrapper=new LambdaQueryWrapper<>();

        if(beginTime.isEmpty()){
            LocalDate today = LocalDate.now();
            LocalDateTime beginDay = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

            queryWrapper.like(KpiRulePercent::getName,name)
                    .orderByAsc(KpiRulePercent::getId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay);
        }
        else {
            LocalDateTime beginDay = LocalDateTime.of(LocalDate.parse(beginTime), LocalTime.MIN);
            LocalDateTime endDay = LocalDateTime.of(LocalDate.parse(endTime), LocalTime.MAX);

            queryWrapper.like(KpiRulePercent::getName,name)
                    .orderByAsc(KpiRulePercent::getId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay)
                    .apply(StringUtils.checkValNotNull(endDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endDay);
        }
        KpiRulePercentService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/upload")
    public R uploadExcel(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), KpiExcel.class, new KpiExcelReadListener()).sheet().doRead();
        return R.success();
    }

    @PostMapping("/uploadPosition")
    public R uploadPositionExcel(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), PositionKpiExcel.class, new PositionKpiExcelReadListener()).sheet().doRead();
        return R.success();
    }

}
