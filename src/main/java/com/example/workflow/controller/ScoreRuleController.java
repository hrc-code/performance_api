package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.entity.*;
import com.example.workflow.listener.PositionScoreExcelReadListener;
import com.example.workflow.listener.ScoreExcelReadListener;
import com.example.workflow.mapper.PositionScoreMapper;
import com.example.workflow.mapper.PositionScoreViewMapper;
import com.example.workflow.mapper.ScoreContactAssessorsMapper;
import com.example.workflow.mapper.ScoreRuleMapper;
import com.example.workflow.pojo.PositionScoreExcel;
import com.example.workflow.pojo.ScoreExcel;
import com.example.workflow.service.PositionScoreService;
import com.example.workflow.service.ScoreAssessorsService;
import com.example.workflow.service.ScoreRuleService;
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
@RequestMapping("/ScoreRule")
public class ScoreRuleController {
    @Autowired
    private ScoreRuleService ScoreRuleService;
    @Autowired
    private ScoreRuleMapper ScoreRuleMapper;
    @Autowired
    private ScoreContactAssessorsMapper ScoreContactAssessorsMapper;
    @Autowired
    private ScoreAssessorsService ScoreAssessorsService;
    @Autowired
    private PositionScoreMapper PositionScoreMapper;
    @Autowired
    private PositionScoreViewMapper PositionScoreViewMapper;
    @Autowired
    private PositionScoreService PositionScoreService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        Page<ScoreRule> pageInfo=new Page<ScoreRule>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<ScoreRule> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        ScoreRuleService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    @PostMapping("/update")
    private R update(@RequestBody ScoreRule one){
        if(one.getTarget()==null)
            R.error("条目名称不得为空");

        ScoreRuleMapper.updateById(one);

        return R.success();
    }


    @PostMapping("/delete")
    private R delete(@RequestBody ScoreRule one){
        List<PositionScore> list=PositionScoreService.lambdaQuery()
                        .eq(PositionScore::getScoreId,one.getId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        list.forEach(x->{
            List<ScoreAssessors> assessors=ScoreAssessorsService.lambdaQuery()
                    .eq(ScoreAssessors::getPositionScoreId,x.getId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();
            ScoreAssessorsService.removeBatchByIds(assessors);
        });
        PositionScoreService.removeBatchByIds(list);

        ScoreRuleMapper.deleteById(one);

        return R.success();
    }


    @PostMapping("/add")
    private R add(@RequestBody ScoreRule one){
        if(one.getTarget()==null)
            R.error("条目名称不得为空");

        ScoreRuleMapper.insert(one);

        return R.success();
    }


    @PostMapping("/list")
    private R<List<ScoreContactAssessors>> list(@RequestBody JSONObject obj){

        LambdaQueryWrapper<ScoreContactAssessors> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreContactAssessors::getPositionId,obj.getString("id"))
                .orderByAsc(ScoreContactAssessors::getScoreId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<ScoreContactAssessors> list=ScoreContactAssessorsMapper.selectList(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/getRulelist")
    private R<List<ScoreRule>> getRulelist(){

        LambdaQueryWrapper<ScoreRule> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreRule::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<ScoreRule> list=ScoreRuleService.list(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/addAssessor")
    private R addAssessor(@RequestBody ScoreRuleForm form){
        LambdaQueryWrapper<PositionScore> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionScore::getPositionId,form.getPositionId())
                .eq(PositionScore::getScoreId,form.getScoreId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;;

        List<PositionScore> positionScores=PositionScoreService.list(queryWrapper);
        if(positionScores!=null&&!positionScores.isEmpty()){
            return R.error("评分条目已存在，不可重复添加");
        }

        PositionScore Score=ScoreRuleService.splitForm(form);
        PositionScoreMapper.insert(Score);

        List<ScoreAssessors> list=ScoreAssessorsService.splitForm(form,Score.getId());
        ScoreAssessorsService.saveBatch(list);
        return R.success();
    }

    @PostMapping("/getPositionScoreList")
    private R<List<PositionScoreView>> getPositionScoreList(@RequestBody JSONObject obj){

        LambdaQueryWrapper<PositionScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionScoreView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;;
        List<PositionScoreView> list=PositionScoreViewMapper.selectList(queryWrapper);

        return R.success(list);
    }


    @PostMapping("/getPositionScoreAssessorList")
    private R<List<PositionScoreView>> getPositionScoreAssessorList(@RequestBody JSONObject obj){

        LambdaQueryWrapper<PositionScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionScoreView::getPositionId,obj.getString("positionId"))
                .eq(PositionScoreView::getAssessorId,obj.getString("assessorId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;;
        List<PositionScoreView> list=PositionScoreViewMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/getOneScoreAssessorList")
    private R<List<PositionScoreView>> getOneScoreAssessorList(@RequestBody JSONObject obj){

        LambdaQueryWrapper<PositionScoreView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionScoreView::getPositionId,obj.getString("positionId"))
                .eq(PositionScoreView::getScoreId,obj.getString("scoreId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;;
        List<PositionScoreView> list=PositionScoreViewMapper.selectList(queryWrapper);

        return R.success(list);
    }

    @PostMapping("/updateAssessor")
    private R updateAssessor(@RequestBody ScoreRuleForm form){
        LambdaQueryWrapper<PositionScore> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(PositionScore::getPositionId,form.getPositionId())
                .eq(PositionScore::getScoreId,form.getScoreId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<PositionScore> positionScores=PositionScoreService.list(queryWrapper1);

        if(positionScores.size()>1){
            return R.error("评分条目存在重复，不可修改");
        }

        PositionScore positionScore=ScoreRuleService.splitForm(form);
        PositionScoreMapper.updateById(positionScore);

        LambdaQueryWrapper<ScoreAssessors> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreAssessors::getPositionScoreId,positionScore.getId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        ScoreAssessorsService.remove(queryWrapper);

        List<ScoreAssessors> list=ScoreAssessorsService.splitForm(form,positionScore.getId());
        ScoreAssessorsService.saveBatch(list);
        return R.success();
    }


    @PostMapping("/removeAssessor")
    private R removeAssessor(@RequestBody PositionScoreView form){
        LambdaQueryWrapper<PositionScore> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionScore::getPositionId,form.getPositionId())
                .eq(PositionScore::getScoreId,form.getScoreId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<PositionScore> list=PositionScoreService.list(queryWrapper);

        list.forEach(x->{
            LambdaQueryWrapper<ScoreAssessors> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(ScoreAssessors::getPositionScoreId,x.getId())
                    .eq(ScoreAssessors::getState,1)
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);

            ScoreAssessorsService.remove(queryWrapper1);
        });
        PositionScoreService.remove(queryWrapper);

        return R.success();
    }


    @PostMapping("/copy")
    private R copy(){
        List<ScoreRule> list= ScoreRuleService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        if(!list.isEmpty())
            return R.error("本月评分考核条目已复制，请勿重复操作");

        ScoreRuleService.monthCopy();
        return R.success();
    }

    @GetMapping("/scorePastPage")
    public R<Page> scorePastPage(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<ScoreRule> pageInfo=new Page<ScoreRule>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<ScoreRule> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        ScoreRuleService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @GetMapping("/pastSearch")
    public R<Page> pastPage(@RequestParam("page") String page
            ,@RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String target
            ,@RequestParam(defaultValue = "") String beginTime
            ,@RequestParam(defaultValue = "") String endTime){

        Page<ScoreRule> pageInfo=new Page<ScoreRule>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<ScoreRule> queryWrapper=new LambdaQueryWrapper<>();

        if(beginTime.isEmpty()){
            LocalDate today = LocalDate.now();
            LocalDateTime beginDay = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

            queryWrapper.like(ScoreRule::getTarget,target)
                    .orderByAsc(ScoreRule::getId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay);
        }
        else {
            LocalDateTime beginDay = LocalDateTime.of(LocalDate.parse(beginTime), LocalTime.MIN);
            LocalDateTime endDay = LocalDateTime.of(LocalDate.parse(endTime), LocalTime.MAX);

            queryWrapper.like(ScoreRule::getTarget,target)
                    .orderByAsc(ScoreRule::getId)
                    .apply(StringUtils.checkValNotNull(beginDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginDay)
                    .apply(StringUtils.checkValNotNull(endDay),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endDay);
        }
        ScoreRuleService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PostMapping("/upload")
    public R uploadExcel(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), ScoreExcel.class, new ScoreExcelReadListener()).sheet().doRead();
        return R.success();
    }

    @PostMapping("/uploadPosition")
    public R uploadPositionExcel(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), PositionScoreExcel.class, new PositionScoreExcelReadListener()).sheet().doRead();
        return R.success();
    }
}
