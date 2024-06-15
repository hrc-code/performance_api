package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.BackWait;
import com.example.workflow.entity.EmpKpiView;
import com.example.workflow.entity.EmpOkr;
import com.example.workflow.entity.EmpOkrView;
import com.example.workflow.entity.EmpPieceView;
import com.example.workflow.entity.EmpScore;
import com.example.workflow.entity.EmpScoreView;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.OkrKey;
import com.example.workflow.entity.OkrRule;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.PositionScore;
import com.example.workflow.entity.PositionView;
import com.example.workflow.entity.ResultSecondExamine;
import com.example.workflow.entity.Role;
import com.example.workflow.entity.ScoreAssessors;
import com.example.workflow.entity.TaskState;
import com.example.workflow.entity.TaskView;
import com.example.workflow.mapper.EmpKpiViewMapper;
import com.example.workflow.mapper.EmpOkrViewMapper;
import com.example.workflow.mapper.EmpPieceViewMapper;
import com.example.workflow.mapper.EmpScoreViewMapper;
import com.example.workflow.mapper.EmployeeMapper;
import com.example.workflow.mapper.ResultKpiEmpViewMapper;
import com.example.workflow.mapper.ResultPieceEmpViewMapper;
import com.example.workflow.mapper.ResultScoreEmpViewMapper;
import com.example.workflow.mapper.RoleMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.service.BackWaitService;
import com.example.workflow.service.EmpOkrService;
import com.example.workflow.service.EmpScoreService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.EmployeeService;
import com.example.workflow.service.OkrKeyService;
import com.example.workflow.service.OkrRuleService;
import com.example.workflow.service.PositionScoreService;
import com.example.workflow.service.PositionService;
import com.example.workflow.service.PositionViewService;
import com.example.workflow.service.ResultSecondExamineService;
import com.example.workflow.service.RoleService;
import com.example.workflow.service.ScoreAssessorsService;
import com.example.workflow.service.TaskViewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
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

@Slf4j
@RestController
@RequestMapping("/ResultSecondExamine")
public class ResultSecondExamineController {
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ResultScoreEmpViewMapper ResultScoreEmpViewMapper;
    @Autowired
    private ResultPieceEmpViewMapper ResultPieceEmpViewMapper;
    @Autowired
    private ResultKpiEmpViewMapper ResultKpiEmpViewMapper;
    @Autowired
    private RoleMapper RoleMapper;
    @Autowired
    private ResultSecondExamineService ResultSecondExamineService;
    @Autowired
    private EmployeeMapper EmployeeMapper;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private EmpScoreViewMapper EmpScoreViewMapper;
    @Autowired
    private EmpOkrViewMapper EmpOkrViewMapper;
    @Autowired
    private TaskViewService TaskViewService;
    @Autowired
    private BackWaitService BackWaitService;
    @Autowired
    private PositionViewService PositionViewService;
    @Autowired
    private RoleService RoleService;
    @Autowired
    private EmployeeService EmployeeService;
    @Autowired
    private EmpKpiViewMapper EmpKpiViewMapper;
    @Autowired
    private EmpPieceViewMapper EmpPieceViewMapper;
    @Autowired
            private PositionService PositionService;
    @Autowired
            private PositionScoreService PositionScoreService;
    @Autowired
            private ScoreAssessorsService ScoreAssessorsService;
    @Autowired
            private EmpScoreService EmpScoreService;
    @Autowired
            private OkrKeyService OkrKeyService;
    @Autowired
            private OkrRuleService OkrRuleService;
    @Autowired
            private EmpOkrService EmpOkrService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @PostMapping("/completeList")
    private R<List<PositionView>> completeList(@RequestBody JSONObject obj){
        List<ResultSecondExamine> resultThirdExamineList=ResultSecondExamineService.lambdaQuery()
                .eq(ResultSecondExamine::getAssessorId, obj.getString("assessorId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        List<PositionView> list=new ArrayList<>();
        resultThirdExamineList.forEach(x->{
            PositionView empPositionView= PositionViewService.lambdaQuery()
                    .eq(PositionView::getId,x.getPositionId())
                    .eq(PositionView::getState,1)
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

        ResultSecondExamine resultSecondExamine=ResultSecondExamineService.lambdaQuery()
                .eq(ResultSecondExamine::getAssessorId, obj.getString("assessorId"))
                .eq(ResultSecondExamine::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        if(resultSecondExamine.getKpiExamine()==1){
            state.setKpiState(1);

            LambdaQueryWrapper<EmpKpiView> queryWrapper3=new LambdaQueryWrapper<>();
            queryWrapper3.eq(EmpKpiView::getPositionId,obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
            List<EmpKpiView> list3= EmpKpiViewMapper.selectList(queryWrapper3);
            List<Object> kpi = new ArrayList<>(list3);
            resultMap.put("kpi", kpi);
        }

        if(resultSecondExamine.getPieceExamine()==1){
            state.setPieceState(1);

            LambdaQueryWrapper<EmpPieceView> queryWrapper2=new LambdaQueryWrapper<>();
            queryWrapper2.eq(EmpPieceView::getPositionId,obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
            List<EmpPieceView> list2= EmpPieceViewMapper.selectList(queryWrapper2);
            List<Object> piece = new ArrayList<>(list2);
            resultMap.put("piece", piece);
        }

        if(resultSecondExamine.getScoreExamine()==1){
            state.setScoreState(1);

            LambdaQueryWrapper<EmpScoreView> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(EmpScoreView::getPositionId,obj.getString("positionId"))
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
            List<EmpScoreView> list1= EmpScoreViewMapper.selectList(queryWrapper1);
            List<Object> score = new ArrayList<>(list1);
            resultMap.put("score", score);
        }

        if(resultSecondExamine.getOkrExamine()==1){
            state.setOkrState(1);

            LambdaQueryWrapper<EmpOkrView> queryWrapper4=new LambdaQueryWrapper<>();
            queryWrapper4.eq(EmpOkrView::getPositionId,obj.getString("positionId"))
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
        queryWrapper1.eq(EmpScoreView::getPositionId,obj.getString("positionId"))
                .orderByAsc(EmpScoreView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<EmpScoreView> list1= EmpScoreViewMapper.selectList(queryWrapper1);
        if(!list1.isEmpty())
            state.setScoreState(1);

        LambdaQueryWrapper<EmpPieceView> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmpPieceView::getPositionId,obj.getString("positionId"))
                .orderByAsc(EmpPieceView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<EmpPieceView> list2= EmpPieceViewMapper.selectList(queryWrapper2);
        if(!list2.isEmpty())
            state.setPieceState(1);

        LambdaQueryWrapper<EmpKpiView> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(EmpKpiView::getPositionId,obj.getString("positionId"))
                .orderByAsc(EmpKpiView::getEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        List<EmpKpiView> list3= EmpKpiViewMapper.selectList(queryWrapper3);
        if(!list3.isEmpty())
            state.setKpiState(1);

        LambdaQueryWrapper<EmpOkrView> queryWrapper4=new LambdaQueryWrapper<>();
        queryWrapper4.eq(EmpOkrView::getPositionId,obj.getString("positionId"))
                .orderByAsc(EmpOkrView::getLiaEmpId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpOkrView> list4= EmpOkrViewMapper.selectList(queryWrapper4);
        if(!list4.isEmpty())
            state.setOkrState(1);

        if(state.getKpiState().equals(0)
                &&state.getOkrState().equals(0)
                &&state.getPieceState().equals(0)
                &&state.getScoreState().equals(0)){
            state.setTotalState(1);
        }
        else {
            state.setTotalState(0);
        }

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
    private R add(@RequestBody JSONObject form){
        LambdaQueryWrapper<EmployeePosition> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(EmployeePosition::getPositionId,form.getString("positionId"));
        List<EmployeePosition> empList= EmployeePositionService.list(Wrapper);

        JSONObject formArray = form.getJSONObject("form");

        ResultSecondExamine result=new ResultSecondExamine();
        result.setAssessorId(Long.valueOf(form.getString("assessorId")));
        result.setPositionId(Long.valueOf(form.getString("positionId")));
        result.setScoreExamine(Short.valueOf(String.valueOf(formArray.get("scoreExamine"))));
        result.setPieceExamine(Short.valueOf(String.valueOf(formArray.get("pieceExamine"))));
        result.setKpiExamine(Short.valueOf(String.valueOf(formArray.get("kpiExamine"))));
        result.setOkrExamine(Short.valueOf(String.valueOf(formArray.get("okrExamine"))));
        ResultSecondExamineService.save(result);


        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();
        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

        if(!result.getOkrExamine().equals(Short.parseShort("2"))
                &&!result.getKpiExamine().equals(Short.parseShort("2"))
                &&!result.getPieceExamine().equals(Short.parseShort("2"))
                &&!result.getScoreExamine().equals(Short.parseShort("2"))){
            empList.forEach(x->{
                LambdaQueryWrapper<TaskView> queryWrapper1=new LambdaQueryWrapper<>();
                queryWrapper1.eq(TaskView::getAssignee,form.getString("assessorId"))
                        .eq(TaskView::getStartUserId,x.getEmpId())
                        .eq(TaskView::getProcInstId,x.getProcessDefinitionId())
                        .eq(TaskView::getName,"second")
                        .eq(TaskView::getState,"ACTIVE");
                TaskView task=TaskViewMapper.selectOne(queryWrapper1);

                Map<String,Object> map = new HashMap<>();
                map.put("wage_emp",empId.toString());

                taskService.complete(task.getId(),map);

                Position position=PositionService.lambdaQuery()
                        .eq(Position::getId,form.getString("positionId"))
                        .eq(Position::getState,1)
                        .one();
                position.setAuditStatus(Short.parseShort("3"));
                PositionService.updateById(position);
            });
        }
        else{
            empList.forEach(x->{
                LambdaQueryWrapper<TaskView> queryWrapper1=new LambdaQueryWrapper<>();
                queryWrapper1.eq(TaskView::getAssignee,form.getString("assessorId"))
                        .eq(TaskView::getStartUserId,x.getEmpId())
                        .eq(TaskView::getProcInstId,x.getProcessDefinitionId())
                        .eq(TaskView::getName,"second")
                        .eq(TaskView::getState,"ACTIVE");
                TaskView task=TaskViewMapper.selectOne(queryWrapper1);

                runtimeService.suspendProcessInstanceById(task.getProcInstId());

                Integer positionType=Integer.valueOf(form.getString("positionType"));

                if(result.getOkrExamine().equals(Short.parseShort("2"))){
                    removeOkr(form,x);
                    if(positionType.equals(5)){
                        ResultSecondExamineService.reOkrSecondFifth(form,x);
                    }
                    else if(positionType.equals(4)||positionType.equals(3)){
                        ResultSecondExamineService.reOkrSecondFourth(form,x);
                    }
                }
                if(result.getKpiExamine().equals(Short.parseShort("2"))){
                    if(positionType.equals(5)){
                        ResultSecondExamineService.reKpiSecondFifth(form);
                    }
                    else if(positionType.equals(4)){
                        ResultSecondExamineService.reKpiSecondFourth(form);
                    }
                    else if(positionType.equals(3)){
                        ResultSecondExamineService.reKpiSecondThird(form);
                    }
                }
                if(result.getPieceExamine().equals(Short.parseShort("2"))){
                    if(positionType.equals(5)){
                        ResultSecondExamineService.rePieceSecondFifth(form);
                    }
                    else if(positionType.equals(4)){
                        ResultSecondExamineService.rePieceSecondFourth(form);
                    }
                    else if(positionType.equals(3)){
                        ResultSecondExamineService.rePieceSecondThird(form);
                    }
                }
                if(result.getScoreExamine().equals(Short.parseShort("2"))){
                    removeScore(form,x);
                    if(positionType.equals(5)){
                        ResultSecondExamineService.reScoreSecondFifth(form);
                    }
                    else if(positionType.equals(4)||positionType.equals(3)){
                        ResultSecondExamineService.reScoreSecondFourth(form);
                    }
                }
            });
        }
        return R.success();
    }

    @PostMapping("/getSecondBackOkrTask")
    private R<List<PositionView>> getSecondBackOkrTask(@RequestBody JSONObject obj){
        List<TaskView> task= TaskViewService.lambdaQuery()
                .eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"back_second_okr")
                .eq(TaskView::getState,"ACTIVE")
                .list();

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            backWaitList.add(
                    BackWaitService.lambdaQuery()
                    .eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId())
                    .eq(BackWait::getType,"back_second_okr")
                            .one());
        });
        Map<Long, Integer> countMap1 = new HashMap<>();
        for (BackWait backWait : backWaitList) {
            Long positionId = backWait.getPositionId();
            countMap1.put(positionId, countMap1.getOrDefault(positionId, 0) + 1);
        }

        List<BackWait> allList= BackWaitService.lambdaQuery()
                .eq(BackWait::getType,"back_second_okr")
                .list();
        Map<Long, Integer> countMap2 = new HashMap<>();
        for (BackWait backWait : allList) {
            Long positionId = backWait.getPositionId();
            countMap2.put(positionId, countMap2.getOrDefault(positionId, 0) + 1);
        }

        List<Long> samePositionIds = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : countMap1.entrySet()) {
            Long positionId = entry.getKey();
            Integer count1 = entry.getValue();
            Integer count2 = countMap2.get(positionId);

            if (count2 != null && count1.equals(count2)) {
                samePositionIds.add(positionId);
            }
        }

        List<PositionView> positionViewList=new ArrayList<>();
        samePositionIds.forEach(x->{
            positionViewList.add(PositionViewService.lambdaQuery()
                    .eq(PositionView::getId,x)
                    .one());
        });
        return R.success(positionViewList);
    }

    @PostMapping("/getSecondBackPieceTask")
    private R<List<PositionView>> getSecondBackPieceTask(@RequestBody JSONObject obj){
        List<TaskView> task= TaskViewService.lambdaQuery()
                .eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"back_second_piece")
                .eq(TaskView::getState,"ACTIVE")
                .list();

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            backWaitList.add(
                    BackWaitService.lambdaQuery()
                            .eq(BackWait::getEmpId,x.getStartUserId())
                            .eq(BackWait::getProcessDefineId,x.getProcInstId())
                            .eq(BackWait::getType,"back_second_piece")
                            .one());
        });
        Map<Long, Integer> countMap1 = new HashMap<>();
        for (BackWait backWait : backWaitList) {
            Long positionId = backWait.getPositionId();
            countMap1.put(positionId, countMap1.getOrDefault(positionId, 0) + 1);
        }

        List<BackWait> allList= BackWaitService.lambdaQuery()
                .eq(BackWait::getType,"back_second_piece")
                .list();
        Map<Long, Integer> countMap2 = new HashMap<>();
        for (BackWait backWait : allList) {
            Long positionId = backWait.getPositionId();
            countMap2.put(positionId, countMap2.getOrDefault(positionId, 0) + 1);
        }

        List<Long> samePositionIds = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : countMap1.entrySet()) {
            Long positionId = entry.getKey();
            Integer count1 = entry.getValue();
            Integer count2 = countMap2.get(positionId);

            if (count2 != null && count1.equals(count2)) {
                samePositionIds.add(positionId);
            }
        }

        List<PositionView> positionViewList=new ArrayList<>();
        samePositionIds.forEach(x->{
            positionViewList.add(PositionViewService.lambdaQuery()
                    .eq(PositionView::getId,x)
                    .one());
        });
        return R.success(positionViewList);
    }

    @PostMapping("/getSecondBackScoreTask")
    private R<List<PositionView>> getSecondBackScoreTask(@RequestBody JSONObject obj){
        List<TaskView> task= TaskViewService.lambdaQuery()
                .eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"back_second_score")
                .eq(TaskView::getState,"ACTIVE")
                .list();

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            backWaitList.add(
                    BackWaitService.lambdaQuery()
                            .eq(BackWait::getEmpId,x.getStartUserId())
                            .eq(BackWait::getProcessDefineId,x.getProcInstId())
                            .eq(BackWait::getType,"back_second_score")
                            .one());
        });
        Map<Long, Integer> countMap1 = new HashMap<>();
        for (BackWait backWait : backWaitList) {
            Long positionId = backWait.getPositionId();
            countMap1.put(positionId, countMap1.getOrDefault(positionId, 0) + 1);
        }

        List<BackWait> allList= BackWaitService.lambdaQuery()
                .eq(BackWait::getType,"back_second_score")
                .list();
        Map<Long, Integer> countMap2 = new HashMap<>();
        for (BackWait backWait : allList) {
            Long positionId = backWait.getPositionId();
            countMap2.put(positionId, countMap2.getOrDefault(positionId, 0) + 1);
        }

        List<Long> samePositionIds = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : countMap1.entrySet()) {
            Long positionId = entry.getKey();
            Integer count1 = entry.getValue();
            Integer count2 = countMap2.get(positionId);

            if (count2 != null && count1.equals(count2)) {
                samePositionIds.add(positionId);
            }
        }

        List<PositionView> positionViewList=new ArrayList<>();
        samePositionIds.forEach(x->{
            positionViewList.add(PositionViewService.lambdaQuery()
                    .eq(PositionView::getId,x)
                    .one());
        });
        return R.success(positionViewList);
    }

    @PostMapping("/getSecondBackKpiTask")
    private R<List<PositionView>> getSecondBackKpiTask(@RequestBody JSONObject obj){
        List<TaskView> task= TaskViewService.lambdaQuery()
                .eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"back_second_kpi")
                .eq(TaskView::getState,"ACTIVE")
                .list();

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            backWaitList.add(
                    BackWaitService.lambdaQuery()
                            .eq(BackWait::getEmpId,x.getStartUserId())
                            .eq(BackWait::getProcessDefineId,x.getProcInstId())
                            .eq(BackWait::getType,"back_second_kpi")
                            .one());
        });
        Map<Long, Integer> countMap1 = new HashMap<>();
        for (BackWait backWait : backWaitList) {
            Long positionId = backWait.getPositionId();
            countMap1.put(positionId, countMap1.getOrDefault(positionId, 0) + 1);
        }

        List<BackWait> allList= BackWaitService.lambdaQuery()
                .eq(BackWait::getType,"back_second_kpi")
                .list();
        Map<Long, Integer> countMap2 = new HashMap<>();
        for (BackWait backWait : allList) {
            Long positionId = backWait.getPositionId();
            countMap2.put(positionId, countMap2.getOrDefault(positionId, 0) + 1);
        }

        List<Long> samePositionIds = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : countMap1.entrySet()) {
            Long positionId = entry.getKey();
            Integer count1 = entry.getValue();
            Integer count2 = countMap2.get(positionId);

            if (count2 != null && count1.equals(count2)) {
                samePositionIds.add(positionId);
            }
        }

        List<PositionView> positionViewList=new ArrayList<>();
        samePositionIds.forEach(x->{
            positionViewList.add(PositionViewService.lambdaQuery()
                    .eq(PositionView::getId,x)
                    .one());
        });
        return R.success(positionViewList);
    }


    @PostMapping("/backAddScore")
    private R backAddScore(@RequestBody JSONObject form){
        ResultSecondExamine examine=ResultSecondExamineService.lambdaQuery()
                .eq(ResultSecondExamine::getPositionId,form.getString("positionId"))
                .eq(ResultSecondExamine::getAssessorId,form.getString("assessorId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        examine.setScoreExamine(Short.parseShort("1"));
        ResultSecondExamineService.updateById(examine);

        List<BackWait> backWaitList=BackWaitService.lambdaQuery()
                .eq(BackWait::getType,"back_second_score")
                .eq(BackWait::getPositionId,form.getString("positionId"))
                .list();

        List<TaskView> taskViews=new ArrayList<>();
        backWaitList.forEach(x->{
            taskViews.add(TaskViewService.lambdaQuery()
                    .eq(TaskView::getAssignee,form.getString("assessorId"))
                    .eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,x.getProcessDefineId())
                    .eq(TaskView::getName,"back_second_score")
                    .one());
        });
        BackWaitService.removeBatchByIds(backWaitList);

        taskViews.forEach(x->{
            taskService.complete(x.getId());
        });

        if(!examine.getOkrExamine().equals(Short.parseShort("2"))
                &&!examine.getKpiExamine().equals(Short.parseShort("2"))
                &&!examine.getPieceExamine().equals(Short.parseShort("2"))
                &&!examine.getScoreExamine().equals(Short.parseShort("2"))){
            updateFlow(form);
        }

        return R.success();
    }

    @PostMapping("/backAddPiece")
    private R backAddPiece(@RequestBody JSONObject form){
        ResultSecondExamine examine=ResultSecondExamineService.lambdaQuery()
                .eq(ResultSecondExamine::getPositionId,form.getString("positionId"))
                .eq(ResultSecondExamine::getAssessorId,form.getString("assessorId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        examine.setPieceExamine(Short.parseShort("1"));
        ResultSecondExamineService.updateById(examine);

        List<BackWait> backWaitList=BackWaitService.lambdaQuery()
                .eq(BackWait::getType,"back_second_piece")
                .eq(BackWait::getPositionId,form.getString("positionId"))
                .list();

        List<TaskView> taskViews=new ArrayList<>();
        backWaitList.forEach(x->{
            taskViews.add(TaskViewService.lambdaQuery()
                    .eq(TaskView::getAssignee,form.getString("assessorId"))
                    .eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,x.getProcessDefineId())
                    .eq(TaskView::getName,"back_second_piece")
                    .one());
        });
        BackWaitService.removeBatchByIds(backWaitList);

        taskViews.forEach(x->{
            taskService.complete(x.getId());
        });

        if(!examine.getOkrExamine().equals(Short.parseShort("2"))
                &&!examine.getKpiExamine().equals(Short.parseShort("2"))
                &&!examine.getPieceExamine().equals(Short.parseShort("2"))
                &&!examine.getScoreExamine().equals(Short.parseShort("2"))){
            updateFlow(form);
        }

        return R.success();
    }

    @PostMapping("/backAddKpi")
    private R backAddKpi(@RequestBody JSONObject form){
        ResultSecondExamine examine=ResultSecondExamineService.lambdaQuery()
                .eq(ResultSecondExamine::getPositionId,form.getString("positionId"))
                .eq(ResultSecondExamine::getAssessorId,form.getString("assessorId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        examine.setKpiExamine(Short.parseShort("1"));
        ResultSecondExamineService.updateById(examine);

        List<BackWait> backWaitList=BackWaitService.lambdaQuery()
                .eq(BackWait::getType,"back_second_kpi")
                .eq(BackWait::getPositionId,form.getString("positionId"))
                .list();

        List<TaskView> taskViews=new ArrayList<>();
        backWaitList.forEach(x->{
            taskViews.add(TaskViewService.lambdaQuery()
                    .eq(TaskView::getAssignee,form.getString("assessorId"))
                    .eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,x.getProcessDefineId())
                    .eq(TaskView::getName,"back_second_kpi")
                    .one());
        });
        BackWaitService.removeBatchByIds(backWaitList);

        taskViews.forEach(x->{
            taskService.complete(x.getId());
        });

        if(!examine.getOkrExamine().equals(Short.parseShort("2"))
                &&!examine.getKpiExamine().equals(Short.parseShort("2"))
                &&!examine.getPieceExamine().equals(Short.parseShort("2"))
                &&!examine.getScoreExamine().equals(Short.parseShort("2"))){
            updateFlow(form);
        }

        return R.success();
    }

    @PostMapping("/backAddOkr")
    private R backAddOkr(@RequestBody JSONObject form){
        ResultSecondExamine examine=ResultSecondExamineService.lambdaQuery()
                .eq(ResultSecondExamine::getPositionId,form.getString("positionId"))
                .eq(ResultSecondExamine::getAssessorId,form.getString("assessorId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        examine.setOkrExamine(Short.parseShort("1"));
        ResultSecondExamineService.updateById(examine);

        List<BackWait> backWaitList=BackWaitService.lambdaQuery()
                .eq(BackWait::getType,"back_second_okr")
                .eq(BackWait::getPositionId,form.getString("positionId"))
                .list();

        List<TaskView> taskViews=new ArrayList<>();
        backWaitList.forEach(x->{
            taskViews.add(TaskViewService.lambdaQuery()
                    .eq(TaskView::getAssignee,form.getString("assessorId"))
                    .eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,x.getProcessDefineId())
                    .eq(TaskView::getName,"back_second_okr")
                    .one());
        });
        BackWaitService.removeBatchByIds(backWaitList);

        taskViews.forEach(x->{
            taskService.complete(x.getId());
        });

        if(!examine.getOkrExamine().equals(Short.parseShort("2"))
                &&!examine.getKpiExamine().equals(Short.parseShort("2"))
                &&!examine.getPieceExamine().equals(Short.parseShort("2"))
                &&!examine.getScoreExamine().equals(Short.parseShort("2"))){
            updateFlow(form);
        }

        return R.success();
    }


    private void updateFlow(JSONObject form){
        Long roleId= RoleService.lambdaQuery()
                .eq(Role::getRoleName,"绩效专员")
                .eq(Role::getState,1)
                .one().getId();

        Long empId=EmployeeService.lambdaQuery()
                .eq(Employee::getRoleId,roleId)
                .one().getId();

            List<EmployeePosition> empList= EmployeePositionService.lambdaQuery()
                    .eq(EmployeePosition::getPositionId,form.getString("positionId"))
                    .list();

            empList.forEach(x->{
                TaskView task=TaskViewService.lambdaQuery()
                        .eq(TaskView::getAssignee,form.getString("assessorId"))
                        .eq(TaskView::getStartUserId,x.getEmpId())
                        .eq(TaskView::getProcInstId,x.getProcessDefinitionId())
                        .eq(TaskView::getName,"second")
                        .eq(TaskView::getState,"SUSPENDED")
                        .one();
                runtimeService.activateProcessInstanceById(task.getProcInstId());

                Map<String,Object> map = new HashMap<>();
                map.put("wage_emp",empId.toString());
                taskService.complete(task.getId(),map);
            });

        Position position=PositionService.lambdaQuery()
                .eq(Position::getId,form.getString("positionId"))
                .eq(Position::getState,1)
                .one();
        position.setAuditStatus(Short.parseShort("3"));
        PositionService.updateById(position);
    }

    private void removeScore(JSONObject form,EmployeePosition one){
        List<String> assessorList1 = new ArrayList<>();
        List<PositionScore> scoreList=PositionScoreService.lambdaQuery()
                .eq(PositionScore::getPositionId,form.getString("positionId"))
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
                queryWrapper.eq(EmpScore::getEmpId,one.getEmpId())
                        .eq(EmpScore::getScoreAssessorsId,y.getId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                EmpScoreService.remove(queryWrapper);
            });
        });
    }

    private void removeOkr(JSONObject form,EmployeePosition one){
        List<OkrKey> okrList=OkrKeyService.lambdaQuery()
                .eq(OkrKey::getPositionId,form.getString("positionId"))
                .eq(OkrKey::getLiaEmpId,one.getEmpId())
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
                LambdaQueryWrapper<EmpOkr> queryWrapper=new LambdaQueryWrapper<>();
                queryWrapper.eq(EmpOkr::getOkrKeyId,x.getId())
                        .eq(EmpOkr::getEmpId,one.getEmpId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
                EmpOkrService.remove(queryWrapper);
            });
        });
    }

    /*@PostMapping("/page")
    public R<Map<String, List<Object>>> page(@RequestBody JSONObject obj){

        LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId,obj.getString("positionId"));
        List<EmployeePosition> EmpList= EmployeePositionService.list(queryWrapper);

        List<Map<String, List<Object>>> result=new ArrayList<>();
        EmpList.forEach(x->{
            TaskState state=new TaskState();
            state.setKpiState(0);
            state.setPieceState(0);
            state.setOkrState(0);
            state.setScoreState(0);

            LambdaQueryWrapper<ResultScoreEmpView> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(ResultScoreEmpView::getEmpId,x.getEmpId())
                    .eq(ResultScoreEmpView::getPositionId,obj.getString("positionId"));
            List<ResultScoreEmpView> list1= ResultScoreEmpViewMapper.selectList(queryWrapper1);
            if(list1.isEmpty()!=true)
                state.setScoreState(1);

            LambdaQueryWrapper<ResultPieceEmpView> queryWrapper2=new LambdaQueryWrapper<>();
            queryWrapper2.eq(ResultPieceEmpView::getEmpId,x.getEmpId())
                    .eq(ResultPieceEmpView::getPositionId,obj.getString("positionId"));
            List<ResultPieceEmpView> list2= ResultPieceEmpViewMapper.selectList(queryWrapper2);
            if(list2.isEmpty()!=true)
                state.setPieceState(1);

            LambdaQueryWrapper<ResultKpiEmpView> queryWrapper3=new LambdaQueryWrapper<>();
            queryWrapper3.eq(ResultKpiEmpView::getEmpId,x.getEmpId())
                    .eq(ResultKpiEmpView::getPositionId,obj.getString("positionId"));
            List<ResultKpiEmpView> list3= ResultKpiEmpViewMapper.selectList(queryWrapper3);
            if(list3.isEmpty()!=true)
                state.setKpiState(1);

            List<Object> score = new ArrayList<>(list1);
            List<Object> piece = new ArrayList<>(list2);
            List<Object> kpi = new ArrayList<>(list3);
            List<Object> taskState = new ArrayList<>();
            taskState.add(state);

            Map<String, List<Object>> resultMap = new HashMap<>();
            resultMap.put("score", score);
            resultMap.put("piece", piece);
            resultMap.put("kpi", kpi);
            resultMap.put("state", taskState);

            result.add(resultMap);
        });

        Map<String, List<Object>> object=new HashMap<>();
        List<Object> records = new ArrayList<>(result);
        List<Object> total=new ArrayList<>();
        total.add(records.size());
        List<Object> pages=new ArrayList<>();
        pages.add(1);
        String num="1";

        object.put("records", records);
        object.put("total", total);
        object.put("pages", pages);

        return R.success(object);
    }*/
}
