package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.listener.PieceExcelReadListener;
import com.example.workflow.listener.PositionPieceExcelReadListener;
import com.example.workflow.mapper.PieceRuleMapper;
import com.example.workflow.mapper.PositionPieceMapper;
import com.example.workflow.mapper.PositionPieceViewMapper;
import com.example.workflow.model.entity.PieceRule;
import com.example.workflow.model.entity.PositionPiece;
import com.example.workflow.model.entity.PositionPieceView;
import com.example.workflow.model.feedback.ErrorExcelWrite;
import com.example.workflow.model.feedback.PieceError;
import com.example.workflow.model.feedback.PositionPieceError;
import com.example.workflow.model.pojo.PieceExcel;
import com.example.workflow.model.pojo.PositionPieceExcel;
import com.example.workflow.service.PieceRuleService;
import com.example.workflow.service.PositionPieceService;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/PieceRule")
public class PieceRuleController {
    @Autowired
    private PieceRuleService PieceRuleService;
    @Autowired
    private PieceRuleMapper PieceRuleMapper;
    @Autowired
    private PositionPieceMapper PositionPieceMapper;
    @Autowired
    private PositionPieceViewMapper PositionPieceViewMapper;
    @Autowired
    private PositionPieceService PositionPieceService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        Page<PieceRule> pageInfo=new Page<PieceRule>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<PieceRule> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        PieceRuleService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @PostMapping("/update")
    private R update(@RequestBody PieceRule one){
        if(one.getName()==null)
            return R.error("条目名称不得为空");
        else if(one.getTarget1()==null)
            return R.error("条目一名称不得为空");
        else if(one.getTarget2()==null)
            return R.error("条目二名称不得为空");
        else if(one.getTargetNum()==null)
            return R.error("条目一单价不得为空");

        PieceRuleMapper.updateById(one);

        return R.success();
    }


    @PostMapping("/delete")
    private R delete(@RequestBody PieceRule one){
        List<PositionPiece> list=PositionPieceService.lambdaQuery()
                        .eq(PositionPiece::getPieceId,one.getId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        PositionPieceService.removeBatchByIds(list);

        PieceRuleMapper.deleteById(one);

        return R.success();
    }


    @PostMapping("/add")
    private R add(@RequestBody PieceRule one){
        if(one.getName()==null)
            return R.error("条目名称不得为空");
        else if(one.getTarget1()==null)
            return R.error("条目一名称不得为空");
        else if(one.getTarget2()==null)
            return R.error("条目二名称不得为空");
        else if(one.getTargetNum()==null)
            return R.error("条目一单价不得为空");

        PieceRule pieceRule=PieceRuleService.lambdaQuery()
                .eq(PieceRule::getName,one.getName())
                .eq(PieceRule::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        if(pieceRule!=null){
            R.error("该计件条目已存在，请勿重复添加");
        }

        PieceRuleMapper.insert(one);

        return R.success();
    }


    @PostMapping("/list")
    private R<List<PositionPieceView>> list(@RequestBody JSONObject obj){

        LambdaQueryWrapper<PositionPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionPieceView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<PositionPieceView> list=PositionPieceViewMapper.selectList(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/getRuleList")
    private R<List<PieceRule>> getRulelist(){
        LambdaQueryWrapper<PieceRule> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PieceRule::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<PieceRule> list=PieceRuleService.list(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/addAssessor")
    private R addAssessor(@RequestBody PositionPiece form){
        LambdaQueryWrapper<PositionPiece> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionPiece::getPositionId,form.getPositionId())
                        .eq(PositionPiece::getPieceId,form.getPieceId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ;
        List<PositionPiece> positionPieces=PositionPieceService.list(queryWrapper);
        if(positionPieces!=null&&!positionPieces.isEmpty()){
            return R.error("计件条目已存在，不可重复添加");
        }

        PositionPieceMapper.insert(form);
        return  R.success();
    }


    @PostMapping("/getPositionPieceList")
    private R<List<PositionPieceView>> getPositionPieceList(@RequestBody JSONObject obj){

        LambdaQueryWrapper<PositionPieceView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionPieceView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<PositionPieceView> list=PositionPieceViewMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/updateAssessor")
    private R updateAssessor(@RequestBody PositionPiece form){

        LambdaQueryWrapper<PositionPiece> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionPiece::getPositionId,form.getPositionId())
                .eq(PositionPiece::getPieceId,form.getPieceId())
                .eq(PositionPiece::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<PositionPiece> positionPieces=PositionPieceService.list(queryWrapper);

        if(positionPieces.size()>1){
            return R.error("计件条目重复存在，不可修改");
        }

        PositionPieceMapper.updateById(form);
        return  R.success();
    }


    @PostMapping("/removeAssessor")
    private R removeAssessor(@RequestBody PositionPieceView form){

        LambdaQueryWrapper<PositionPiece> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionPiece::getPositionId,form.getPositionId())
                .eq(PositionPiece::getPieceId,form.getPieceId())
                .eq(PositionPiece::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        PositionPieceService.remove(queryWrapper);

        return R.success();
    }


    @PostMapping("/copy")
    private R copy(){
        List<PieceRule> list= PieceRuleService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(!list.isEmpty())
            return R.error("本月计件考核条目已复制，请勿重复操作");

        PieceRuleService.monthCopy();

        return R.success();
    }


    @GetMapping("/piecePastPage")
    public R<Page> piecePastPage(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<PieceRule> pageInfo=new Page<PieceRule>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<PieceRule> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        PieceRuleService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @GetMapping("/pastSearch")
    public R<Page> pastPage(@RequestParam("page") String page
            ,@RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String name
            ,@RequestParam(defaultValue = "") String beginTime
            ,@RequestParam(defaultValue = "") String endTime){

        Page<PieceRule> pageInfo=new Page<PieceRule>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<PieceRule> queryWrapper=new LambdaQueryWrapper<>();

        if(beginTime.isEmpty()){
            LocalDate today = LocalDate.now();
            LocalDateTime beginDay = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

            queryWrapper.like(PieceRule::getName,name)
                    .orderByAsc(PieceRule::getId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay);
        }
        else {
            LocalDateTime beginDay = LocalDateTime.of(LocalDate.parse(beginTime), LocalTime.MIN);
            LocalDateTime endDay = LocalDateTime.of(LocalDate.parse(endTime), LocalTime.MAX);

            queryWrapper.like(PieceRule::getName,name)
                    .orderByAsc(PieceRule::getId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay)
                    .apply(StringUtils.checkValNotNull(endDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endDay);
        }
        PieceRuleService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/upload")
    public void uploadExcel(MultipartFile file, HttpServletResponse response) throws IOException {
        EasyExcel.read(file.getInputStream(), PieceExcel.class, new PieceExcelReadListener()).sheet().doRead();

        if(!ErrorExcelWrite.getErrorCollection().isEmpty()){
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

            EasyExcel.write(response.getOutputStream(), PieceError.class).sheet("错误部分").doWrite(ErrorExcelWrite.getErrorCollection());
        }
        ErrorExcelWrite.clearErrorCollection();
    }

    @PostMapping("/uploadPosition")
    public void uploadPositionExcel(MultipartFile file,HttpServletResponse response) throws IOException {

        EasyExcel.read(file.getInputStream(), PositionPieceExcel.class, new PositionPieceExcelReadListener()).sheet().doRead();
        if(!ErrorExcelWrite.getErrorCollection().isEmpty()){
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

            EasyExcel.write(response.getOutputStream(), PositionPieceError.class).sheet("错误部分").doWrite(ErrorExcelWrite.getErrorCollection());
        }
        ErrorExcelWrite.clearErrorCollection();
    }
}
