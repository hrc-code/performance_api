package com.example.workflow.controller;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.*;
import com.example.workflow.mapper.EmpKpiViewMapper;
import com.example.workflow.mapper.EmpOkrViewMapper;
import com.example.workflow.mapper.EmpPieceViewMapper;
import com.example.workflow.mapper.EmpScoreViewMapper;
import com.example.workflow.mapper.ResultKpiEmpViewMapper;
import com.example.workflow.mapper.ResultPieceEmpViewMapper;
import com.example.workflow.mapper.ResultScoreEmpViewMapper;
import com.example.workflow.mapper.ResultThirdExamineMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/ResultThirdExamine")
public class ResultThirdExamineController {
    @Autowired
    private ResultScoreEmpViewMapper ResultScoreEmpViewMapper;
    @Autowired
    private ResultPieceEmpViewMapper ResultPieceEmpViewMapper;
    @Autowired
    private ResultKpiEmpViewMapper ResultKpiEmpViewMapper;
    @Autowired
    private ResultThirdExamineService ResultThirdExamineService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ResultThirdExamineMapper ResultThirdExamineMapper;
    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private EmpScoreViewMapper EmpScoreViewMapper;
    @Autowired
    private EmpOkrViewMapper EmpOkrViewMapper;
    @Autowired
            private PositionScoreService PositionScoreService;
    @Autowired
            private ScoreAssessorsService ScoreAssessorsService;
    @Autowired
            private IdentityService identityService;
    @Autowired
            private RuntimeService runtimeService;
    @Autowired
            private PositionPieceService PositionPieceService;
    @Autowired
            private RoleService RoleService;
    @Autowired
            private EmployeeService EmployeeService;
    @Autowired
            private OkrRuleService OkrRuleService;
    @Autowired
            private OkrKeyService OkrKeyService;
    @Autowired
            private BackWaitService BackWaitService;
    @Autowired
            private EmpScoreViewService EmpScoreViewService;
    @Autowired
            private EmpPieceViewService EmpPieceViewService;
    @Autowired
            private EmpOkrViewService EmpOkrViewService;
    @Autowired
            private EmpKpiViewService EmpKpiViewService;
    @Autowired
            private EmpScoreService EmpScoreService;
    @Autowired
            private PieceRuleService PieceRuleService;
    @Autowired
            private EmpPieceService EmpPieceService;
    @Autowired
            private PositionKpiSerivce PositionKpiSerivce;
    @Autowired
            private EmpKpiService EmpKpiService;
    @Autowired
            private EmpOkrService EmpOkrService;
    @Autowired
            private EmpPieceViewMapper EmpPieceViewMapper;
    @Autowired
            private EmpKpiViewMapper EmpKpiViewMapper;
    @Autowired
            private EmpPositionViewService EmpPositionViewService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @PostMapping("/completeList")
    private R<List<EmpPositionView>> completeList(@RequestBody JSONObject obj){
        List<ResultThirdExamine> resultThirdExamineList=ResultThirdExamineService.lambdaQuery()
                .eq(ResultThirdExamine::getAssessorId, obj.getString("assessorId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        List<EmpPositionView> list=new ArrayList<>();
        resultThirdExamineList.forEach(x->{
            EmpPositionView empPositionView= EmpPositionViewService.lambdaQuery()
                    .eq(EmpPositionView::getPositionId,x.getPositionId())
                    .eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getState,1)
                    .one();
            list.add(empPositionView);
        });

        return R.success(list);
    }

    @PostMapping("/getFinishState")
    private R<Map<String,List<Object>>> getFinishState(@RequestBody JSONObject obj){
        Map<String, List<Object>> resultMap = new HashMap<>();
        TaskState state=new TaskState();
        state.setKpiState(0);
        state.setPieceState(0);
        state.setOkrState(0);
        state.setScoreState(0);

        ResultThirdExamine resultThirdExamine=ResultThirdExamineService.lambdaQuery()
                .eq(ResultThirdExamine::getAssessorId, obj.getString("assessorId"))
                .eq(ResultThirdExamine::getPositionId,obj.getString("positionId"))
                .eq(ResultThirdExamine::getEmpId,obj.getString("empId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        if(resultThirdExamine.getKpiExamine()==1){
            state.setKpiState(1);

            LambdaQueryWrapper<EmpKpiView> queryWrapper3=new LambdaQueryWrapper<>();
            queryWrapper3.eq(EmpKpiView::getEmpId,obj.getString("empId"))
                    .eq(EmpKpiView::getPositionId,obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
            List<EmpKpiView> list3= EmpKpiViewMapper.selectList(queryWrapper3);
            List<Object> kpi = new ArrayList<>(list3);
            resultMap.put("kpi", kpi);
        }

        if(resultThirdExamine.getPieceExamine()==1){
            state.setPieceState(1);

            LambdaQueryWrapper<EmpPieceView> queryWrapper2=new LambdaQueryWrapper<>();
            queryWrapper2.eq(EmpPieceView::getEmpId,obj.getString("empId"))
                    .eq(EmpPieceView::getPositionId,obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
            List<EmpPieceView> list2= EmpPieceViewMapper.selectList(queryWrapper2);
            List<Object> piece = new ArrayList<>(list2);
            resultMap.put("piece", piece);
        }

        if(resultThirdExamine.getScoreExamine()==1){
            state.setScoreState(1);

            LambdaQueryWrapper<EmpScoreView> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(EmpScoreView::getEmpId,obj.getString("empId"))
                    .eq(EmpScoreView::getPositionId,obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
            List<EmpScoreView> list1= EmpScoreViewMapper.selectList(queryWrapper1);
            List<Object> score = new ArrayList<>(list1);
            resultMap.put("score", score);
        }

        if(resultThirdExamine.getOkrExamine()==1){
            state.setOkrState(1);

            LambdaQueryWrapper<EmpOkrView> queryWrapper4=new LambdaQueryWrapper<>();
            queryWrapper4.eq(EmpOkrView::getLiaEmpId,obj.getString("empId"))
                    .eq(EmpOkrView::getPositionId,obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
            List<EmpOkrView> list4= EmpOkrViewMapper.selectList(queryWrapper4);
            List<Object> okr=new ArrayList<>(list4);
            resultMap.put("okr",okr);
        }
        List<Object> taskState = new ArrayList<>();
        taskState.add(state);
        resultMap.put("state", taskState);

        return R.success();
    }


    @PostMapping("/list")
    private R<Map<String, List<Object>>> list(@RequestBody JSONObject obj) throws JsonProcessingException {
        TaskState state=new TaskState();
        state.setKpiState(0);
        state.setPieceState(0);
        state.setOkrState(0);
        state.setScoreState(0);

        LambdaQueryWrapper<EmpScoreView> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(EmpScoreView::getEmpId,obj.getString("empId"))
                .eq(EmpScoreView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<EmpScoreView> list1= EmpScoreViewMapper.selectList(queryWrapper1);
        if(!list1.isEmpty())
            state.setScoreState(1);

        LambdaQueryWrapper<EmpPieceView> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmpPieceView::getEmpId,obj.getString("empId"))
                .eq(EmpPieceView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<EmpPieceView> list2= EmpPieceViewMapper.selectList(queryWrapper2);
        if(!list2.isEmpty())
            state.setPieceState(1);

        LambdaQueryWrapper<EmpKpiView> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(EmpKpiView::getEmpId,obj.getString("empId"))
                .eq(EmpKpiView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<EmpKpiView> list3= EmpKpiViewMapper.selectList(queryWrapper3);
        if(!list3.isEmpty())
            state.setKpiState(1);

        LambdaQueryWrapper<EmpOkrView> queryWrapper4=new LambdaQueryWrapper<>();
        queryWrapper4.eq(EmpOkrView::getLiaEmpId,obj.getString("empId"))
                .eq(EmpOkrView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpOkrView> list4= EmpOkrViewMapper.selectList(queryWrapper4);
        if(!list4.isEmpty())
            state.setOkrState(1);

        List<Object> score = new ArrayList<>(list1);
        List<Object> piece = new ArrayList<>(list2);
        List<Object> kpi = new ArrayList<>(list3);
        List<Object> okr=new ArrayList<>(list4);
        List<Object> taskState = new ArrayList<>();
        taskState.add(state);

        Map<String, List<Object>> resultMap = new HashMap<>();
        resultMap.put("score", score);
        resultMap.put("piece", piece);
        resultMap.put("kpi", kpi);
        resultMap.put("okr",okr);
        resultMap.put("state", taskState);

        return R.success(resultMap);
    }


    @PostMapping("/add")
    private R add(@RequestBody ResultThirdExamineForm form){
        ResultThirdExamine examin= new ResultThirdExamine();
        BeanUtil.copyProperties(form, examin);
        ResultThirdExamineService.save(examin);

        if(form.getScoreExamine()!=2&&form.getPieceExamine()!=2&&form.getKpiExamine()!=2&&form.getOkrExamine()!=2){
            continueFlow(examin);
        }
        else{
            suspendFlow(form);
        }
        return R.success();
    }


    @PostMapping("/addBackScore")
    private R addBack(@RequestBody JSONObject obj){
        Long empId=Long.valueOf(obj.getString("empId"));
        Long positionId=Long.valueOf(obj.getString("positionId"));

        ResultThirdExamine examine= ResultThirdExamineService.lambdaQuery()
                .eq(ResultThirdExamine::getEmpId,empId)
                .eq(ResultThirdExamine::getPositionId,positionId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        examine.setScoreExamine(new Short("1"));
        ResultThirdExamineService.updateById(examine);

        LambdaQueryWrapper<BackWait> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(BackWait::getEmpId,empId)
                .eq(BackWait::getPositionId,positionId)
                .eq(BackWait::getType,"third_score_back");
        BackWait backWait=BackWaitService.getOne(queryWrapper1);

        if(backWait!=null) {
            LambdaQueryWrapper<TaskView> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TaskView::getAssignee, obj.getString("assessorId"));
            queryWrapper.eq(TaskView::getStartUserId, obj.getString("empId"));
            queryWrapper.eq(TaskView::getProcInstId, backWait.getProcessDefineId());
            queryWrapper.eq(TaskView::getName, "back_third_score");
            queryWrapper.eq(TaskView::getState, "ACTIVE");
            TaskView task = TaskViewMapper.selectOne(queryWrapper);
            taskService.complete(task.getId());
            BackWaitService.removeById(backWait);

            if (examine.getScoreExamine() != 2 && examine.getPieceExamine() != 2 && examine.getKpiExamine() != 2 && examine.getOkrExamine() != 2) {
                LambdaQueryWrapper<TaskView> Wrapper = new LambdaQueryWrapper<>();
                Wrapper.eq(TaskView::getStartUserId, empId);
                Wrapper.eq(TaskView::getAssignee, obj.getString("assessorId"));
                Wrapper.eq(TaskView::getName, "third");
                Wrapper.eq(TaskView::getState, "SUSPENDED");
                TaskView sustask = TaskViewMapper.selectOne(Wrapper);
                runtimeService.activateProcessInstanceById(sustask.getProcInstId());

                continueFlow(examine);
            }
        }

        LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(BackWait::getEmpId,empId)
                .eq(BackWait::getPositionId,positionId)
                .eq(BackWait::getType,"back_second_score");
        BackWait backWaitSecond=BackWaitService.getOne(queryWrapper);

        if(backWaitSecond!=null){
            PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                    .eq(PositionAssessor::getPositionId, obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .one();

            Map<String, Object> map = new HashMap<>();
            map.put("Assessor", nextAssessor.getSecondAssessorId().toString());

            LambdaQueryWrapper<TaskView> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(TaskView::getAssignee, obj.getString("assessorId"))
                    .eq(TaskView::getStartUserId, obj.getString("empId"))
                    .eq(TaskView::getProcInstId, backWaitSecond.getProcessDefineId())
                    .eq(TaskView::getName, "back_third_score")
                    .eq(TaskView::getState, "ACTIVE");
            TaskView task = TaskViewMapper.selectOne(queryWrapper2);

            taskService.complete(task.getId(),map);
        }
        return R.success();
    }


    @PostMapping("/addBackPiece")
    private R addBackPiece(@RequestBody JSONObject obj){
        Long empId=Long.valueOf(obj.getString("empId"));
        Long positionId=Long.valueOf(obj.getString("positionId"));

        ResultThirdExamine examine= ResultThirdExamineService.lambdaQuery()
                .eq(ResultThirdExamine::getEmpId,empId)
                .eq(ResultThirdExamine::getPositionId,positionId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        examine.setPieceExamine(new Short("1"));
        ResultThirdExamineService.updateById(examine);

        LambdaQueryWrapper<BackWait> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(BackWait::getEmpId,empId)
                .eq(BackWait::getPositionId,positionId)
                .eq(BackWait::getType,"third_piece_back");
        BackWait backWait=BackWaitService.getOne(queryWrapper1);

        if(backWait!=null) {
            LambdaQueryWrapper<TaskView> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TaskView::getAssignee, obj.getString("assessorId"));
            queryWrapper.eq(TaskView::getStartUserId, obj.getString("empId"));
            queryWrapper.eq(TaskView::getProcInstId, backWait.getProcessDefineId());
            queryWrapper.eq(TaskView::getName, "back_third_piece");
            queryWrapper.eq(TaskView::getState, "ACTIVE");
            TaskView task = TaskViewMapper.selectOne(queryWrapper);
            taskService.complete(task.getId());
            BackWaitService.removeById(backWait);

            if (examine.getScoreExamine() != 2 && examine.getPieceExamine() != 2 && examine.getKpiExamine() != 2 && examine.getOkrExamine() != 2) {
                LambdaQueryWrapper<TaskView> Wrapper = new LambdaQueryWrapper<>();
                Wrapper.eq(TaskView::getStartUserId, empId);
                Wrapper.eq(TaskView::getAssignee, obj.getString("assessorId"));
                Wrapper.eq(TaskView::getName, "third");
                Wrapper.eq(TaskView::getState, "SUSPENDED");
                TaskView sustask = TaskViewMapper.selectOne(Wrapper);
                runtimeService.activateProcessInstanceById(sustask.getProcInstId());
                continueFlow(examine);
            }
        }

        LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(BackWait::getEmpId,empId)
                .eq(BackWait::getPositionId,positionId)
                .eq(BackWait::getType,"back_second_piece");
        BackWait backWaitSecond=BackWaitService.getOne(queryWrapper);

        if(backWaitSecond!=null){
            PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                    .eq(PositionAssessor::getPositionId, obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .one();

            Map<String, Object> map = new HashMap<>();
            map.put("Assessor", nextAssessor.getSecondAssessorId().toString());

            LambdaQueryWrapper<TaskView> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(TaskView::getAssignee, obj.getString("assessorId"))
                    .eq(TaskView::getStartUserId, obj.getString("empId"))
                    .eq(TaskView::getProcInstId, backWaitSecond.getProcessDefineId())
                    .eq(TaskView::getName, "back_third_piece")
                    .eq(TaskView::getState, "ACTIVE");
            TaskView task = TaskViewMapper.selectOne(queryWrapper2);

            taskService.complete(task.getId(),map);
        }
        return R.success();
    }


    @PostMapping("/addBackKpi")
    private R addBackKpi(@RequestBody JSONObject obj){
        Long empId=Long.valueOf(obj.getString("empId"));
        Long positionId=Long.valueOf(obj.getString("positionId"));

        ResultThirdExamine examine= ResultThirdExamineService.lambdaQuery()
                .eq(ResultThirdExamine::getEmpId,empId)
                .eq(ResultThirdExamine::getPositionId,positionId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        examine.setKpiExamine(new Short("1"));
        ResultThirdExamineService.updateById(examine);

        LambdaQueryWrapper<BackWait> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(BackWait::getEmpId,empId)
                .eq(BackWait::getPositionId,positionId)
                .eq(BackWait::getType,"third_kpi_back");
        BackWait backWait=BackWaitService.getOne(queryWrapper1);

        if(backWait!=null) {
            LambdaQueryWrapper<TaskView> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TaskView::getAssignee, obj.getString("assessorId"));
            queryWrapper.eq(TaskView::getStartUserId, obj.getString("empId"));
            queryWrapper.eq(TaskView::getProcInstId, backWait.getProcessDefineId());
            queryWrapper.eq(TaskView::getName, "back_third_kpi");
            queryWrapper.eq(TaskView::getState, "ACTIVE");
            TaskView task = TaskViewMapper.selectOne(queryWrapper);
            taskService.complete(task.getId());
            BackWaitService.removeById(backWait);

            if (examine.getScoreExamine() != 2 && examine.getPieceExamine() != 2 && examine.getKpiExamine() != 2 && examine.getOkrExamine() != 2) {
                LambdaQueryWrapper<TaskView> Wrapper = new LambdaQueryWrapper<>();
                Wrapper.eq(TaskView::getStartUserId, empId);
                Wrapper.eq(TaskView::getAssignee, obj.getString("assessorId"));
                Wrapper.eq(TaskView::getName, "third");
                Wrapper.eq(TaskView::getState, "SUSPENDED");
                TaskView sustask = TaskViewMapper.selectOne(Wrapper);
                runtimeService.activateProcessInstanceById(sustask.getProcInstId());

                continueFlow(examine);
            }
        }

        LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(BackWait::getEmpId,empId)
                .eq(BackWait::getPositionId,positionId)
                .eq(BackWait::getType,"back_second_kpi");
        BackWait backWaitSecond=BackWaitService.getOne(queryWrapper);

        if(backWaitSecond!=null){
            PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                    .eq(PositionAssessor::getPositionId, obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .one();

            Map<String, Object> map = new HashMap<>();
            map.put("Assessor", nextAssessor.getSecondAssessorId().toString());

            LambdaQueryWrapper<TaskView> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(TaskView::getAssignee, obj.getString("assessorId"))
                    .eq(TaskView::getStartUserId, obj.getString("empId"))
                    .eq(TaskView::getProcInstId, backWaitSecond.getProcessDefineId())
                    .eq(TaskView::getName, "back_third_kpi")
                    .eq(TaskView::getState, "ACTIVE");
            TaskView task = TaskViewMapper.selectOne(queryWrapper2);

            taskService.complete(task.getId(),map);

        }
        return R.success();
    }


    @PostMapping("/addBackOkr")
    private R addBackOkr(@RequestBody JSONObject obj){
        Long empId=Long.valueOf(obj.getString("empId"));
        Long positionId=Long.valueOf(obj.getString("positionId"));

        ResultThirdExamine examine= ResultThirdExamineService.lambdaQuery()
                .eq(ResultThirdExamine::getEmpId,empId)
                .eq(ResultThirdExamine::getPositionId,positionId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        examine.setOkrExamine(new Short("1"));
        ResultThirdExamineService.updateById(examine);

        LambdaQueryWrapper<BackWait> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(BackWait::getEmpId,empId)
                .eq(BackWait::getPositionId,positionId)
                .eq(BackWait::getType,"third_okr_back");
        BackWait backWait=BackWaitService.getOne(queryWrapper1);

        if(backWait!=null) {
            LambdaQueryWrapper<TaskView> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TaskView::getAssignee, obj.getString("assessorId"));
            queryWrapper.eq(TaskView::getStartUserId, obj.getString("empId"));
            queryWrapper.eq(TaskView::getProcInstId, backWait.getProcessDefineId());
            queryWrapper.eq(TaskView::getName, "back_third_okr");
            queryWrapper.eq(TaskView::getState, "ACTIVE");
            TaskView task = TaskViewMapper.selectOne(queryWrapper);
            taskService.complete(task.getId());
            BackWaitService.removeById(backWait);

            if (examine.getScoreExamine() != 2 && examine.getPieceExamine() != 2 && examine.getKpiExamine() != 2 && examine.getOkrExamine() != 2) {
                LambdaQueryWrapper<TaskView> Wrapper = new LambdaQueryWrapper<>();
                Wrapper.eq(TaskView::getStartUserId, empId);
                Wrapper.eq(TaskView::getAssignee, obj.getString("assessorId"));
                Wrapper.eq(TaskView::getName, "third");
                Wrapper.eq(TaskView::getState, "SUSPENDED");
                TaskView sustask = TaskViewMapper.selectOne(Wrapper);
                runtimeService.activateProcessInstanceById(sustask.getProcInstId());

                continueFlow(examine);
            }
        }

        LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(BackWait::getEmpId,empId)
                .eq(BackWait::getPositionId,positionId)
                .eq(BackWait::getType,"back_second_okr");
        BackWait backWaitSecond=BackWaitService.getOne(queryWrapper);

        if(backWaitSecond!=null){
            PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                    .eq(PositionAssessor::getPositionId, obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .one();

            Map<String, Object> map = new HashMap<>();
            map.put("Assessor", nextAssessor.getSecondAssessorId().toString());

            LambdaQueryWrapper<TaskView> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(TaskView::getAssignee, obj.getString("assessorId"))
                    .eq(TaskView::getStartUserId, obj.getString("empId"))
                    .eq(TaskView::getProcInstId, backWaitSecond.getProcessDefineId())
                    .eq(TaskView::getName, "back_third_okr")
                    .eq(TaskView::getState, "ACTIVE");
            TaskView task = TaskViewMapper.selectOne(queryWrapper2);

            taskService.complete(task.getId(),map);

        }
        return R.success();
    }


    @PostMapping("/backList")
    private R<Map<String, List<Object>>> backList(@RequestBody JSONObject obj) throws JsonProcessingException {
        Long empId=Long.valueOf(obj.getString("empId"));
        Long positionId=Long.valueOf(obj.getString("positionId"));

        LambdaQueryWrapper<ResultThirdExamine> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ResultThirdExamine::getEmpId,obj.getString("empId"));
        ResultThirdExamine one= ResultThirdExamineMapper.selectOne(queryWrapper);

        TaskState state=new TaskState();
        state.setKpiState(one.getKpiExamine()==1?0:1);
        state.setPieceState(one.getPieceExamine()==1?0:1);
        state.setOkrState(one.getOkrExamine()==1?0:1);
        state.setScoreState(one.getScoreExamine()==1?0:1);
        Map<String, List<Object>> resultMap = new HashMap<>();
        if(state.getScoreState()==1){
            List<EmpScoreView> list1= EmpScoreViewService.lambdaQuery()
                    .eq(EmpScoreView::getEmpId,empId)
                    .eq(EmpScoreView::getPositionId,positionId)
                    .list();

            List<Object> score = new ArrayList<>(list1);
            resultMap.put("score", score);
        }

        if(state.getPieceState()==1){
            List<EmpPieceView> list2=EmpPieceViewService.lambdaQuery()
                    .eq(EmpPieceView::getEmpId,empId)
                    .eq(EmpPieceView::getPositionId,positionId)
                    .list();

            List<Object> piece = new ArrayList<>(list2);
            resultMap.put("piece", piece);
        }

        if(state.getKpiState()==1){
            List<EmpKpiView> list3=EmpKpiViewService.lambdaQuery()
                    .eq(EmpKpiView::getEmpId,empId)
                    .eq(EmpKpiView::getPositionId,positionId)
                    .list();

            List<Object> kpi = new ArrayList<>(list3);
            resultMap.put("kpi", kpi);
        }

        if(state.getOkrState()==1){
            List<EmpOkrView> list4=EmpOkrViewService.lambdaQuery()
                    .eq(EmpOkrView::getLiaEmpId,empId)
                    .eq(EmpOkrView::getPositionId,positionId)
                    .list();

            List<Object> okr = new ArrayList<>(list4);
            resultMap.put("okr", okr);
        }

        List<Object> taskState = new ArrayList<>();
        taskState.add(state);
        resultMap.put("state", taskState);

        return R.success(resultMap);
    }


    public void suspendFlow(ResultThirdExamineForm form){
        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();
        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        if(form.getScoreExamine()==2){
            List<String> assessorList1 = new ArrayList<>();
            List<PositionScore> scoreList=PositionScoreService.lambdaQuery()
                            .eq(PositionScore::getPositionId,form.getPositionId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();
            scoreList.forEach(x->{
                List<ScoreAssessors> assessor= ScoreAssessorsService.lambdaQuery()
                        .eq(ScoreAssessors::getPositionScoreId,x.getId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .list();
                assessor.forEach(y->{
                    assessorList1.add(String.valueOf(y.getAssessorId()));

                    LambdaQueryWrapper<EmpScore> queryWrapper=new LambdaQueryWrapper<>();
                    queryWrapper.eq(EmpScore::getEmpId,form.getEmpId())
                            .eq(EmpScore::getScoreAssessorsId,y.getId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                    EmpScoreService.remove(queryWrapper);
                });
            });
            List<String> assessors1=assessorList1.stream().distinct().collect(Collectors.toList());
            Map<String,Object> map = new HashMap<>();
            map.put("ASList", assessors1);

            identityService.setAuthenticatedUserId(String.valueOf(form.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_12mkpug",map);

            BackWait backWait=new BackWait();
            backWait.setProcessDefineId(processInstance.getId());
            backWait.setType("third_score_back");
            backWait.setEmpId(form.getEmpId());
            backWait.setPositionId(form.getPositionId());
            backWait.setProcessKey("Process_12mkpug");
            backWait.setOpinion(form.getOpinion1());
            BackWaitService.save(backWait);
        }

        if(form.getPieceExamine()==2){
            Map<String,Object> map = new HashMap<>();
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(form.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0qrsna2",map);

            BackWait backWait=new BackWait();
            backWait.setProcessDefineId(processInstance.getId());
            backWait.setType("third_piece_back");
            backWait.setEmpId(form.getEmpId());
            backWait.setPositionId(form.getPositionId());
            backWait.setProcessKey("Process_0qrsna2");
            backWait.setOpinion(form.getOpinion2());
            BackWaitService.save(backWait);

            List<PositionPiece> pieceRules=PositionPieceService.lambdaQuery()
                    .eq(PositionPiece::getPositionId,form.getPositionId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();

            /*pieceRules.forEach(x->{
                LambdaQueryWrapper<EmpPiece> queryWrapper=new LambdaQueryWrapper<>();
                queryWrapper.eq(EmpPiece::getPieceId,x.getPieceId())
                        .eq(EmpPiece::getEmpId,form.getEmpId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                EmpPieceService.remove(queryWrapper);
            });*/
        }

        if(form.getKpiExamine()==2){
            Map<String,Object> map = new HashMap<>();
            map.put("wageEmp",empId.toString());

            identityService.setAuthenticatedUserId(String.valueOf(form.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_07oa6pv",map);

            BackWait backWait=new BackWait();
            backWait.setProcessDefineId(processInstance.getId());
            backWait.setType("third_kpi_back");
            backWait.setEmpId(form.getEmpId());
            backWait.setPositionId(form.getPositionId());
            backWait.setProcessKey("Process_07oa6pv");
            backWait.setOpinion(form.getOpinion3());
            BackWaitService.save(backWait);

            /*List<PositionKpi> kpiRules=PositionKpiSerivce.lambdaQuery()
                    .eq(PositionKpi::getPositionId,form.getPositionId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();

            kpiRules.forEach(x->{
                LambdaQueryWrapper<EmpKpi> queryWrapper=new LambdaQueryWrapper<>();
                queryWrapper.eq(EmpKpi::getKpiId,x.getKpiId())
                        .eq(EmpKpi::getEmpId,form.getEmpId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                EmpKpiService.remove(queryWrapper);
            });*/
        }

        if(form.getOkrExamine()==2){
            List<String> assessorList2 = new ArrayList<>();
            List<OkrKey> okrList=OkrKeyService.lambdaQuery()
                    .eq(OkrKey::getPositionId,form.getPositionId())
                    .eq(OkrKey::getLiaEmpId,form.getEmpId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();

            okrList.forEach(x->{
                List<OkrRule> assessor= OkrRuleService.lambdaQuery()
                        .eq(OkrRule::getAssessorId,x.getId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .list();
                assessor.forEach(y->{
                    assessorList2.add(String.valueOf(y.getAssessorId()));

                    LambdaQueryWrapper<EmpOkr> queryWrapper=new LambdaQueryWrapper<>();
                    queryWrapper.eq(EmpOkr::getOkrKeyId,x.getId())
                            .eq(EmpOkr::getEmpId,form.getEmpId())
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                    EmpOkrService.remove(queryWrapper);
                });
            });
            List<String> assessors2=assessorList2.stream().distinct().collect(Collectors.toList());
            Map<String,Object> map = new HashMap<>();
            map.put("AOList", assessors2);

            identityService.setAuthenticatedUserId(String.valueOf(form.getEmpId()));
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey("Process_0quk6t8",map);

            BackWait backWait=new BackWait();
            backWait.setProcessDefineId(processInstance.getId());
            backWait.setType("third_okr_back");
            backWait.setEmpId(form.getEmpId());
            backWait.setPositionId(form.getPositionId());
            backWait.setProcessKey("Process_0quk6t8");
            backWait.setOpinion(form.getOpinion4());
            BackWaitService.save(backWait);
        }

        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getStartUserId,form.getEmpId());
        Wrapper.eq(TaskView::getAssignee,form.getAssessorId());
        Wrapper.eq(TaskView::getName,"third");
        Wrapper.eq(TaskView::getState,"ACTIVE");
        TaskView task=TaskViewMapper.selectOne(Wrapper);
        runtimeService.suspendProcessInstanceById(task.getProcInstId());
    }


    public void continueFlow(ResultThirdExamine form){
        LambdaQueryWrapper<EmployeePosition> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getEmpId,form.getEmpId())
                .eq(EmployeePosition::getPositionId,form.getPositionId());
        EmployeePosition EmployeePosition=EmployeePositionService.getOne(queryWrapper2);

        LambdaQueryWrapper<TaskView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee,form.getAssessorId())
                .eq(TaskView::getStartUserId,form.getEmpId())
                .eq(TaskView::getName,"third")
                .eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId());
        TaskView task=TaskViewMapper.selectOne(queryWrapper);

        LambdaQueryWrapper<PositionAssessor> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(PositionAssessor::getPositionId,form.getPositionId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;;
        PositionAssessor nextAssessor= PositionAssessorService.getOne(queryWrapper1);

        Map<String,Object> map = new HashMap<>();
        map.put("secondAssessor",nextAssessor.getSecondAssessorId().toString());
        map.put("secondTimer",nextAssessor.getSecondTimer());

        taskService.complete(task.getId(),map);
    }
}
