package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.listener.EmpPieceExcelReadListener;
import com.example.workflow.mapper.EmpPieceMapper;
import com.example.workflow.model.entity.EmpPiece;
import com.example.workflow.model.entity.EmpPieceView;
import com.example.workflow.model.feedback.EmpPieceError;
import com.example.workflow.model.feedback.ErrorExcelWrite;
import com.example.workflow.model.pojo.EmpPieceExcel;
import com.example.workflow.model.pojo.ResultEmpPieceExcel;
import com.example.workflow.service.EmpPieceService;
import com.example.workflow.service.EmpPieceViewService;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/downLoad")
    private void downLoad(HttpServletResponse response) throws IOException {
        List<EmpPieceView> list=EmpPieceViewService.lambdaQuery()
                .orderByAsc(EmpPieceView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        List<ResultEmpPieceExcel> result=new ArrayList<>();
        list.forEach(x->{
            ResultEmpPieceExcel one=new ResultEmpPieceExcel();
            BeanUtils.copyProperties(x,one);
            result.add(one);
        });

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

        EasyExcel.write(response.getOutputStream(), ResultEmpPieceExcel.class)
                .sheet("导出")
                .doWrite(result);
    }

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

        System.out.println("计件结算开始");
        Page<EmpPieceView> pageInfo=new Page<EmpPieceView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(EmpPieceView::getEmpId)
                .ne(EmpPieceView::getState,0)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpPieceViewService.page(pageInfo,queryWrapper);

        System.out.println("计件结算结束");
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
                .ne(EmpPieceView::getState,0)
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
                .ne(EmpPieceView::getState,0)
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
        if(beginTime.isEmpty()){
            LocalDate today = LocalDate.now();
            LocalDateTime beginDay = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

            queryWrapper.like(EmpPieceView::getEmpId,empId)
                    .like(EmpPieceView::getEmpName,empName)
                    .ne(EmpPieceView::getState,0)
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
                    .ne(EmpPieceView::getState,0)
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
        for(EmpPiece x:form){
            if(x.getEmpId()==null)
                R.error("员工姓名不得为空");
            else if(x.getPieceId()==null)
                R.error("计件条目不得为空");
            else if(x.getWorkOrder()==null)
                R.error("数量不得为空");
            else if(x.getQuality()==null)
                R.error("质量不得为空");
        }
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
        one.setState(Short.parseShort("0"));
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

    @GetMapping("/Search")
    public R<Page> Serach(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String workOrder
            ,@RequestParam(defaultValue = "") String name
            ,@RequestParam(defaultValue = "") String empName){

        Page<EmpPieceView> pageInfo=new Page<EmpPieceView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<EmpPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(EmpPieceView::getWorkOrder,workOrder)
                .like(EmpPieceView::getName,name)
                .like(EmpPieceView::getEmpName,empName)
                .orderByAsc(EmpPieceView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpPieceViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/upload")
    public void uploadExcel(MultipartFile file, HttpServletResponse response) throws IOException {
        EasyExcel.read(file.getInputStream(), EmpPieceExcel.class, new EmpPieceExcelReadListener()).sheet().doRead();

        if(!ErrorExcelWrite.getErrorCollection().isEmpty()){
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

            EasyExcel.write(response.getOutputStream(), EmpPieceError.class).sheet("错误部分").doWrite(ErrorExcelWrite.getErrorCollection());
        }
        ErrorExcelWrite.clearErrorCollection();
    }
}
