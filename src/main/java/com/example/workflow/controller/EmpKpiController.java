package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.entity.*;
import com.example.workflow.feedback.EmpKpiError;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.feedback.PositionScoreError;
import com.example.workflow.listener.EmpKpiExcelReadListener;
import com.example.workflow.listener.EmpPieceExcelReadListener;
import com.example.workflow.mapper.KpiPercentMapper;
import com.example.workflow.pojo.EmpKpiExcel;
import com.example.workflow.pojo.EmpPieceExcel;
import com.example.workflow.pojo.EmpScoreExcel;
import com.example.workflow.pojo.ResultEmpKpiExcel;
import com.example.workflow.service.EmpKpiService;
import com.example.workflow.service.EmpKpiViewService;
import com.example.workflow.service.KpiPercentService;
import com.example.workflow.service.KpiRuleService;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.BeanUtils;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/EmpKpi")
public class EmpKpiController {
    @Autowired
    private KpiPercentMapper KpiPercentMapper;
    @Autowired
    private EmpKpiService EmpKpiService;
    @Autowired
    private EmpKpiViewService EmpKpiViewService;
    @Autowired
            private KpiRuleService KpiRuleService;
    @Autowired
            private KpiPercentService KpiPercentService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @PostMapping("/downLoad")
    private void downLoad(HttpServletResponse response) throws IOException {
        List<EmpKpiView> list=EmpKpiViewService.lambdaQuery()
                .orderByAsc(EmpKpiView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        List<ResultEmpKpiExcel> result=new ArrayList<>();
        list.forEach(x->{
            ResultEmpKpiExcel one=new ResultEmpKpiExcel();
            BeanUtils.copyProperties(x,one);
            result.add(one);
        });

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

        EasyExcel.write(response.getOutputStream(), ResultEmpKpiExcel.class)
                .sheet("导出")
                .doWrite(result);
    }

    @PostMapping("/match")
    private R matchCoefficient(@RequestBody List<EmpKpi> form){
        Long kpiRuleId=form.get(0).getKpiId();

        KpiRule kpiRule= KpiRuleService.lambdaQuery()
                .eq(KpiRule::getId,kpiRuleId)
                .eq(KpiRule::getState,1)
                .one();

        if(kpiRule.getType()==1){
            LambdaQueryWrapper<KpiPercent> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(KpiPercent::getKpiKey)
                    .eq(KpiPercent::getKpiId,kpiRuleId)
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
            List<KpiPercent> list=KpiPercentMapper.selectList(queryWrapper);

            form.forEach(x->{
                String rules=EmpKpiService.defineRule1(list,x.getInTarget2());

                KieHelper helper = new KieHelper();
                helper.addContent(rules, ResourceType.DRL);
                KieSession kSession = helper.build().newKieSession();

                if(x.getEmpId()==null)
                    //!
                    R.error("员工姓名不得为空");
                else if(x.getInTarget1()==null)
                    R.error("kpi条目一不得为空");
                else if(x.getInTarget2()==null)
                    R.error("kpi条目二不得为空");
                else if(x.getKpiId()==null)
                    R.error("kpi项目不得为空");

                Order3 order = new Order3();
                order.setInTarget1(x.getInTarget1());
                order.setInTarget2(x.getInTarget2());

                kSession.insert(order);
                kSession.fireAllRules();

                x.setResult(BigDecimal.valueOf(order.getOutNum()));
                kSession.dispose();
            });
            EmpKpiService.saveBatch(form);
        }
        else if(kpiRule.getType()==2){
            KpiPercent kpiPercent= KpiPercentService.lambdaQuery()
                    .eq(KpiPercent::getKpiId,kpiRuleId)
                    .eq(KpiPercent::getState,1)
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .one();

            form.forEach(x->{
                BigDecimal result=EmpKpiService.defineRule2(kpiPercent,x.getInTarget1(),x.getInTarget2());

                x.setResult(result);
            });
            EmpKpiService.saveBatch(form);

        }

        return R.success();
    }


    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){

        Page<EmpKpiView> pageInfo=new Page<EmpKpiView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpKpiView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpKpiViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @PostMapping("/list")
    private R<List<EmpKpiView>> list(@RequestBody JSONObject obj){

        LambdaQueryWrapper<EmpKpiView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpKpiView::getEmpId,obj.getString("empId"))
            .eq(EmpKpiView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpKpiView> list=EmpKpiViewService.list(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/delete")
    private R delete(@RequestBody EmpKpi one){
        EmpKpiService.removeById(one);
        return R.success();
    }

    @PostMapping("/addBackKpi")
    private R addBackPiece(List<EmpKpi> form){
        EmpKpiService.updateBatchById(form);

        return R.success();
    }


    @PostMapping("/update")
    private R updateMatch(@RequestBody EmpKpi form){

        EmpKpiService.removeById(form);

        KpiRule kpiRule= KpiRuleService.lambdaQuery()
                .eq(KpiRule::getId,form.getKpiId())
                .eq(KpiRule::getState,1)
                .one();

        if(kpiRule.getType()==1) {
            LambdaQueryWrapper<KpiPercent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(KpiPercent::getKpiKey)
                    .eq(KpiPercent::getKpiId, form.getKpiId());
            List<KpiPercent> list = KpiPercentMapper.selectList(queryWrapper);

            String rules = EmpKpiService.defineRule1(list, form.getInTarget2());

            KieHelper helper = new KieHelper();
            helper.addContent(rules, ResourceType.DRL);
            KieSession kSession = helper.build().newKieSession();

            if (form.getEmpId() == null)
                R.error("员工姓名不得为空");
            else if (form.getInTarget1() == null)
                R.error("kpi条目一不得为空");
            else if (form.getInTarget2() == null)
                R.error("kpi条目二不得为空");
            else if (form.getKpiId() == null)
                R.error("kpi项目不得为空");

            Order3 order = new Order3();
            order.setInTarget1(form.getInTarget1());
            order.setInTarget2(form.getInTarget2());

            kSession.insert(order);
            kSession.fireAllRules();

            form.setResult(BigDecimal.valueOf(order.getOutNum()));
            kSession.dispose();
        }
        else if(kpiRule.getType()==2){
            KpiPercent kpiPercent= KpiPercentService.lambdaQuery()
                    .eq(KpiPercent::getKpiId,form.getKpiId())
                    .eq(KpiPercent::getState,1)
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .one();

                BigDecimal result=EmpKpiService.defineRule2(kpiPercent,form.getInTarget1(),form.getInTarget2());
                form.setResult(result);
        }

        EmpKpiService.save(form);
        return R.success();
    }


    @GetMapping("/reAdd")
    private R reAdd(@RequestParam("id") String id
            ,@RequestParam("correctedValue") String correctedValue
            ,@RequestParam("empId") String empId){
        UpdateWrapper<EmpKpi> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("id",id)
                .set("corrected_value",correctedValue);
        EmpKpiService.update(updateWrapper);

        EmpKpiService.reChange(Long.valueOf(id),Long.valueOf(empId));
        return R.success();
    }

    @PostMapping("/reDelete")
    private R reAdd(@RequestBody EmpKpi one){
        one.setState(Short.parseShort("0"));
        EmpKpiService.updateById(one);
        return R.success();
    }

    @PostMapping("/secondBack")
    private R<List<EmpKpiView>> secondBack(@RequestBody JSONObject obj){
        List<EmpKpiView> EmpKpiViews=EmpKpiViewService.lambdaQuery()
                .eq(EmpKpiView::getPositionId,obj.getString("positionId"))
                .orderByAsc(EmpKpiView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(EmpKpiViews);
    }

    @GetMapping("/nowPage")
    public R<Page> nowPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize){

        Page<EmpKpiView> pageInfo=new Page<EmpKpiView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpKpiView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(EmpKpiView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpKpiViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/nowSearch")
    public R<Page> nowSerach(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String empId
            ,@RequestParam(defaultValue = "") String empName){

        Page<EmpKpiView> pageInfo=new Page<EmpKpiView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpKpiView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(EmpKpiView::getEmpId,empId)
                .like(EmpKpiView::getEmpName,empName)
                .orderByAsc(EmpKpiView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpKpiViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/pastPage")
    public R<Page> pastPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<EmpKpiView> pageInfo=new Page<EmpKpiView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpKpiView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(EmpKpiView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        EmpKpiViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @GetMapping("/pastSearch")
    public R<Page> pastPage(@RequestParam("page") String page
            ,@RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String empId
            ,@RequestParam(defaultValue = "") String empName
            ,@RequestParam(defaultValue = "") String beginTime
            ,@RequestParam(defaultValue = "") String endTime){

        Page<EmpKpiView> pageInfo=new Page<EmpKpiView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpKpiView> queryWrapper=new LambdaQueryWrapper<>();
        if(beginTime.equals("")){
            LocalDate today = LocalDate.now();
            LocalDateTime beginDay = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

            queryWrapper.like(EmpKpiView::getEmpId,empId)
                    .like(EmpKpiView::getEmpName,empName)
                    .orderByAsc(EmpKpiView::getEmpId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        }
        else {
            LocalDateTime beginDay = LocalDateTime.of(LocalDate.parse(beginTime), LocalTime.MIN);
            LocalDateTime endDay = LocalDateTime.of(LocalDate.parse(endTime), LocalTime.MAX);

            queryWrapper.like(EmpKpiView::getEmpId,empId)
                    .like(EmpKpiView::getEmpName,empName)
                    .orderByAsc(EmpKpiView::getEmpId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay)
                    .apply(StringUtils.checkValNotNull(endDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endDay);
        }
        EmpKpiViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/upload")
    public void uploadExcel(MultipartFile file, HttpServletResponse response) throws IOException {
        EasyExcel.read(file.getInputStream(), EmpKpiExcel.class, new EmpKpiExcelReadListener()).sheet().doRead();

        if(!ErrorExcelWrite.getErrorCollection().isEmpty()){
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

            EasyExcel.write(response.getOutputStream(), EmpKpiError.class).sheet("错误部分").doWrite(ErrorExcelWrite.getErrorCollection());
        }
        ErrorExcelWrite.clearErrorCollection();
    }
}
