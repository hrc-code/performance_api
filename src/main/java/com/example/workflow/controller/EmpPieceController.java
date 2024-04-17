package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.entity.EmpPiece;
import com.example.workflow.entity.EmpPieceView;
import com.example.workflow.mapper.EmpPieceMapper;
import com.example.workflow.service.EmpPieceService;
import com.example.workflow.service.EmpPieceViewService;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/EmpPiece")
public class EmpPieceController {
    @Autowired
    private EmpPieceService EmpPieceService;
    @Autowired
    private EmpPieceMapper EmpPieceMapper;
    @Autowired
    private EmpPieceViewService EmpPieceViewService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){

        Page<EmpPieceView> pageInfo=new Page<EmpPieceView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpPieceViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/nowPage")
    public R<Page> nowPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize){

        Page<EmpPieceView> pageInfo=new Page<EmpPieceView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(EmpPieceView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpPieceViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/nowSearch")
    public R<Page> nowSerach(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String empId
            ,@RequestParam(defaultValue = "") String empName){

        Page<EmpPieceView> pageInfo=new Page<EmpPieceView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(EmpPieceView::getEmpId,empId)
                .like(EmpPieceView::getEmpName,empName)
                .orderByAsc(EmpPieceView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpPieceViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/pastPage")
    public R<Page> pastPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<EmpPieceView> pageInfo=new Page<EmpPieceView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(EmpPieceView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        EmpPieceViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @GetMapping("/pastSearch")
    public R<Page> pastPage(@RequestParam("page") String page
            ,@RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String empId
            ,@RequestParam(defaultValue = "") String empName
            ,@RequestParam(defaultValue = "") String beginTime
            ,@RequestParam(defaultValue = "") String endTime){

        Page<EmpPieceView> pageInfo=new Page<EmpPieceView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpPieceView> queryWrapper=new LambdaQueryWrapper<>();
        if(beginTime.equals("")){
            LocalDate today = LocalDate.now();
            LocalDateTime beginDay = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

            queryWrapper.like(EmpPieceView::getEmpId,empId)
                    .like(EmpPieceView::getEmpName,empName)
                    .orderByAsc(EmpPieceView::getEmpId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        }
        else {
            LocalDateTime beginDay = LocalDateTime.of(LocalDate.parse(beginTime), LocalTime.MIN);
            LocalDateTime endDay = LocalDateTime.of(LocalDate.parse(endTime), LocalTime.MAX);

            queryWrapper.like(EmpPieceView::getEmpId,empId)
                    .like(EmpPieceView::getEmpName,empName)
                    .orderByAsc(EmpPieceView::getEmpId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay)
                    .apply(StringUtils.checkValNotNull(endDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endDay);
        }
        EmpPieceViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @PostMapping("/list")
    private R<List<EmpPieceView>> list(@RequestBody JSONObject obj){

        LambdaQueryWrapper<EmpPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpPieceView::getEmpId,obj.getString("empId"))
                .eq(EmpPieceView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpPieceView> list=EmpPieceViewService.list(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/add")
    private R add(@RequestBody List<EmpPiece> form){
        EmpPieceService.saveBatch(form);
        return R.success();
    }


    @PostMapping("/updateOne")
    private R updateOne(@RequestBody EmpPiece one){
        EmpPieceService.updateById(one);
        return R.success();
    }


    @PostMapping("/updateAll")
    private R updateAll(List<List<EmpPiece>> form){
        List<EmpPiece> list=new ArrayList<>();
        Long empId=form.get(0).get(0).getEmpId();

        LambdaQueryWrapper<EmpPiece> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpPiece::getEmpId,empId)
                .apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpPieceMapper.delete(queryWrapper);

        form.forEach(x->{
            x.forEach(y->{
                list.add(y);
            });
        });
        EmpPieceService.saveBatch(list);
        return R.success();
    }


    @PostMapping("/delete")
    private R delete(@RequestBody EmpPiece one){
        EmpPieceService.removeById(one);
        return R.success();
    }

    @PostMapping("/deleteAll")
    private R deleteAll(@RequestBody List<EmpPiece> list){
        EmpPieceService.removeBatchByIds(list);
        return R.success();
    }

    @PostMapping("/addBackPiece")
    private R addBackPiece(List<EmpPiece> form){
        EmpPieceService.updateBatchById(form);

        return R.success();
    }


    @PostMapping("/timeList")
    private R<Set<String>> timeList(){
        List<EmpPieceView> list=EmpPieceViewService.list();
        List<String> time=new ArrayList<>();
        list.forEach(x->{
            time.add(x.getCreateTime().toString());
        });

        Set<String> uniqueYearMonths = new HashSet<>();
        for (String dateTimeString : time) {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String yearMonth = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            uniqueYearMonths.add(yearMonth);
        }

        return R.success(uniqueYearMonths);
    }

    @GetMapping("/searchByTime")
    private R<Page> searchByTime(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){

        Page<EmpPieceView> pageInfo=new Page<EmpPieceView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpPieceViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @PostMapping("/reDelete")
    private R reAdd(@RequestBody EmpPiece one){
        one.setState(new Short("0"));
        EmpPieceService.updateById(one);

        EmpPieceService.reChange(one.getId(),one.getEmpId());
        return R.success();
    }


    @PostMapping("/secondBack")
    private R<List<EmpPieceView>> secondBack(@RequestBody JSONObject obj){
        List<EmpPieceView> empPieces=EmpPieceViewService.lambdaQuery()
                .eq(EmpPieceView::getPositionId,obj.getString("positionId"))
                .orderByAsc(EmpPieceView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(empPieces);
    }

    public void updateFlow(Long empId){

    }
}
