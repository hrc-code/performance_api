package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.workflow.common.R;
import com.example.workflow.entity.BackWait;
import com.example.workflow.entity.EmpPositionView;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.PositionView;
import com.example.workflow.entity.TaskState;
import com.example.workflow.entity.TaskView;
import com.example.workflow.mapper.BackWaitMapper;
import com.example.workflow.mapper.EmpPositionViewMapper;
import com.example.workflow.mapper.EmployeePositionMapper;
import com.example.workflow.mapper.PositionViewMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.service.BackWaitService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.PositionViewService;
import com.example.workflow.service.TaskViewService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/TaskView")
public class TaskViewController {
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private EmpPositionViewMapper EmpPositionViewMapper;
    @Autowired
    private EmployeePositionMapper EmployeePositionMapper;
    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private PositionViewMapper PositionViewMapper;
    @Autowired
    private BackWaitMapper BackWaitMapper;
    @Autowired
            private PositionViewService PositionViewService;
    @Autowired
            private TaskViewService TaskViewService;
    @Autowired
            private BackWaitService BackWaitService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @PostMapping("/getWageTask")
    private R<List<EmpPositionView>> getWageTask(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getName,"declare")
                .eq(TaskView::getAssignee,obj.getString("empId"));
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<EmpPositionView> empList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmployeePosition::getProcessDefinitionId,x.getProcInstId());
            EmployeePosition one=EmployeePositionMapper.selectOne(queryWrapper);

            LambdaQueryWrapper<EmpPositionView> query=new LambdaQueryWrapper<>();
            query.eq(EmpPositionView::getEmpId,one.getEmpId())
                    .eq(EmpPositionView::getPositionId,one.getPositionId());
            empList.add(EmpPositionViewMapper.selectOne(query));
        });
        return R.success(empList);
    }


    @PostMapping("/getFourthTask")
    private R<List<EmpPositionView>> getView(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getState,"ACTIVE")
                .eq(TaskView::getName,"piece")
                .and(qw -> qw.eq(TaskView::getName, "piece")
                        .or().eq(TaskView::getName, "kpi"));
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<TaskView> taskList = task.stream()
                .collect(Collectors.toMap(TaskView::getEmpName, Function.identity(), (oldValue, newValue) -> oldValue))
                .values()
                .stream()
                .distinct()
                .collect(Collectors.toList());

        List<EmpPositionView> empList=new ArrayList<>();
        taskList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper2=new LambdaQueryWrapper<>();
            queryWrapper2.eq(EmpPositionView::getProcessDefinitionId,x.getProcInstId())
                    .eq(EmpPositionView::getEmpId,x.getStartUserId());
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper2));
        });
        return R.success(empList);
    }


    @PostMapping("/getFourthTaskState")
    private R<TaskState> getFourthTaskState(@RequestBody JSONObject obj){
        TaskState state=new TaskState();
        state.setKpiState(0);
        state.setPieceState(0);
        state.setOkrState(0);
        state.setScoreState(0);

        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getStartUserId,obj.getString("stateUserId"))
                .eq(TaskView::getAssignee,obj.getString("assessorId"));
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        task.forEach(x->{
            if(x.getName().equals("kpi"))
                state.setKpiState(1);
            if(x.getName().equals("piece"))
                state.setPieceState(1);
        });
        return R.success(state);
    }

    @PostMapping("/getFourthEnterTask")
    private R<List<EmpPositionView>> getFourthEnterTask(@RequestBody JSONObject obj){

        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getState,"ACTIVE")
                .and(qw -> qw.eq(TaskView::getName, "score")
                        .or().eq(TaskView::getName, "okr"));
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<TaskView> taskList = task.stream()
                .collect(Collectors.toMap(TaskView::getEmpName, Function.identity(), (oldValue, newValue) -> oldValue))
                .values()
                .stream()
                .distinct()
                .collect(Collectors.toList());

        List<EmpPositionView> empList=new ArrayList<>();
        taskList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getProcessDefinitionId,x.getProcInstId())
                    .eq(EmpPositionView::getEmpId,x.getStartUserId());
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });

        return R.success(empList);
    }


    @PostMapping("/getFourthTaskEnterState")
    private R<TaskState> getFourthTaskEnterState(@RequestBody JSONObject obj){
        TaskState state=new TaskState();
        state.setKpiState(0);
        state.setPieceState(0);
        state.setOkrState(0);
        state.setScoreState(0);

        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getStartUserId,obj.getString("stateUserId"))
                .eq(TaskView::getAssignee,obj.getString("assessorId"));
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        task.forEach(x->{
            if(x.getName().equals("score"))
                state.setScoreState(1);
            if(x.getName().equals("okr"))
                state.setOkrState(1);
        });
        return R.success(state);
    }


    @PostMapping("/getThirdTask")
    private R<List<EmpPositionView>> getThirdTask(@RequestBody JSONObject obj){

        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"third")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<EmpPositionView> empList=new ArrayList<>();
        task.forEach(x->{
                LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
                queryWrapper.eq(EmpPositionView::getEmpId,x.getStartUserId())
                                .eq(EmpPositionView::getProcessDefinitionId,x.getProcInstId());
                empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }


    @PostMapping("/getSecondTask")
    private R<List<PositionView>> getSecondTask(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"second")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task= TaskViewMapper.selectList(queryWrapper);

        List<Long> positionList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<EmployeePosition> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(EmployeePosition::getEmpId,x.getStartUserId())
                    .eq(EmployeePosition::getProcessDefinitionId,x.getProcInstId());
            positionList.add(EmployeePositionMapper.selectOne(queryWrapper1).getPositionId());
        });

        Map<Long, Integer> countMap = new HashMap<>();
        for (Long num : positionList) {
            countMap.put(num, countMap.getOrDefault(num, 0) + 1);
        }

        List<PositionView> positionViews= PositionViewService.list();
        List<Long> allPosition=new ArrayList<>();
        positionViews.forEach(x->{
            allPosition.add(x.getId());
        });
        Map<Long, Integer> count = new HashMap<>();
        for (Long num : allPosition) {
            count.put(num, count.getOrDefault(num, 0) + 1);
        }

        List<Long> completeList=new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : countMap.entrySet()) {
            Long key = entry.getKey();
            Integer value1 = entry.getValue();
            if (count.containsKey(key) && count.get(key).equals(value1)) {
                completeList.add(key);
            }
        }

        List<PositionView> positionViewList=new ArrayList<>();
        completeList.forEach(x->{
            LambdaQueryWrapper<PositionView> queryWrapper3=new LambdaQueryWrapper<>();
            queryWrapper3.eq(PositionView::getId,x);
            positionViewList.add(PositionViewMapper.selectOne(queryWrapper3));
        });

        return R.success(positionViewList);
    }

    @PostMapping("/getBackWagePiece")
    private R<List<EmpPositionView>> getBackWagePiece(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("empId"))
                .eq(TaskView::getName,"back_wageEmp_piece")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList.add(BackWaitMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        backWaitList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId());
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }

    @PostMapping("/getBackWageKpi")
    private R<List<EmpPositionView>> getBackWageKpi(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("empId"))
                .eq(TaskView::getName,"back_wageEmp_kpi")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList.add(BackWaitMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        backWaitList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId())
                    .eq(EmpPositionView::getState,1);;
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }

    @PostMapping("/getBackFourthScore")
    private R<List<EmpPositionView>> getBackFourthScore(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("empId"))
                .eq(TaskView::getName,"back_score")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList.add(BackWaitMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        backWaitList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId());
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }

    @PostMapping("/getBackFourthOkr")
    private R<List<EmpPositionView>> getBackFourthOkr(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("empId"))
                .eq(TaskView::getName,"back_okr")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList.add(BackWaitMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        backWaitList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId());
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }

    @PostMapping("/getBackFourthPiece")
    private R<List<EmpPositionView>> getBackFourthPiece(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("empId"))
                .eq(TaskView::getName,"back_fourth_piece")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList.add(BackWaitMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        backWaitList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId())
                    .eq(EmpPositionView::getState,1);
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }

    @PostMapping("/getBackFourthKpi")
    private R<List<EmpPositionView>> getBackFourthKpi(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("empId"))
                .eq(TaskView::getName,"back_fourth_kpi")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList.add(BackWaitMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        backWaitList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId())
                    .eq(EmpPositionView::getState,1);;
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }

    @PostMapping("/getBackThirdPiece")
    private R<List<EmpPositionView>> getBackThirdPiece(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("empId"))
                .eq(TaskView::getName,"back_third_piece")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList.add(BackWaitMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        backWaitList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId());
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }

    @PostMapping("/getBackThirdKpi")
    private R<List<EmpPositionView>> getBackThirdKpi(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("empId"))
                .eq(TaskView::getName,"back_third_kpi")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList.add(BackWaitMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        backWaitList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId());
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }

    @PostMapping("/getBackThirdScore")
    private R<List<EmpPositionView>> getBackThirdScore(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("empId"))
                .eq(TaskView::getName,"back_third_score")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList.add(BackWaitMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        backWaitList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId());
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }

    @PostMapping("/getBackThirdOkr")
    private R<List<EmpPositionView>> getBackThirdOkr(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("empId"))
                .eq(TaskView::getName,"back_third_okr")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<BackWait> backWaitList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList.add(BackWaitMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        backWaitList.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId());
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });
        return R.success(empList);
    }


    @PostMapping("/getWageFileTask")
    private R<List<EmpPositionView>> getWageFileTask(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
        Wrapper.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"wage_emp")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task=TaskViewMapper.selectList(Wrapper);

        List<EmployeePosition> list=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmployeePosition::getEmpId,x.getStartUserId())
                    .eq(EmployeePosition::getProcessDefinitionId,x.getProcInstId());
            list.add(EmployeePositionMapper.selectOne(queryWrapper));
        });

        List<EmpPositionView> empList=new ArrayList<>();
        list.forEach(x->{
            LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmpPositionView::getEmpId,x.getEmpId())
                    .eq(EmpPositionView::getPositionId,x.getPositionId());
            empList.add(EmpPositionViewMapper.selectOne(queryWrapper));
        });

        return R.success(empList);
    }


    @PostMapping("/fourthTaskCount")
    private R<Map<String, Integer>> getFourthCount(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper1=new LambdaQueryWrapper<>();
        Wrapper1.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getState,"ACTIVE")
                .and(qw -> qw.eq(TaskView::getName, "piece")
                        .or().eq(TaskView::getName, "kpi"));
        List<TaskView> task1=TaskViewMapper.selectList(Wrapper1);
        List<TaskView> taskList1 = task1.stream()
                .collect(Collectors.toMap(TaskView::getEmpName, Function.identity(), (oldValue, newValue) -> oldValue))
                .values()
                .stream()
                .distinct()
                .collect(Collectors.toList());
        Integer count1 = taskList1.size();

        LambdaQueryWrapper<TaskView> Wrapper2=new LambdaQueryWrapper<>();
        Wrapper2.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getState,"ACTIVE")
                .and(qw -> qw.eq(TaskView::getName, "score")
                        .or().eq(TaskView::getName, "okr"));
        List<TaskView> task2=TaskViewMapper.selectList(Wrapper2);
        List<TaskView> taskList2 = task2.stream()
                .collect(Collectors.toMap(TaskView::getEmpName, Function.identity(), (oldValue, newValue) -> oldValue))
                .values()
                .stream()
                .distinct()
                .collect(Collectors.toList());
        Integer count2 = taskList2.size();

        LambdaQueryWrapper<TaskView> Wrapper3=new LambdaQueryWrapper<>();
        Wrapper3.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .and(qw -> qw.eq(TaskView::getName, "back_score")
                        .or().eq(TaskView::getName, "back_okr"))
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task3=TaskViewMapper.selectList(Wrapper3);
        List<BackWait> backWaitList3=new ArrayList<>();
        task3.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList3.add(BackWaitMapper.selectOne(queryWrapper));
        });
        Integer count3 = backWaitList3.size();

        LambdaQueryWrapper<TaskView> Wrapper4=new LambdaQueryWrapper<>();
        Wrapper4.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .and(qw -> qw.eq(TaskView::getName, "back_fourth_piece")
                        .or().eq(TaskView::getName, "back_fourth_kpi"))
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task4=TaskViewMapper.selectList(Wrapper4);
        List<BackWait> backWaitList4=new ArrayList<>();
        task4.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList4.add(BackWaitMapper.selectOne(queryWrapper));
        });
        Integer count4=backWaitList4.size();

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("count1", count1);
        resultMap.put("count2", count2);
        resultMap.put("count3", count3);
        resultMap.put("count4", count4);

        return R.success(resultMap);
    }


    @PostMapping("/EmpWageTaskCount")
    private R<Map<String, Integer>> getEmpWageCount(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper1=new LambdaQueryWrapper<>();
        Wrapper1.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"wage_emp")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task1=TaskViewMapper.selectList(Wrapper1);
        List<EmployeePosition> list1=new ArrayList<>();
        task1.forEach(x->{
            LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmployeePosition::getEmpId,x.getStartUserId())
                    .eq(EmployeePosition::getProcessDefinitionId,x.getProcInstId());
            list1.add(EmployeePositionMapper.selectOne(queryWrapper));
        });
        Integer count1 = list1.size();

        LambdaQueryWrapper<TaskView> Wrapper2=new LambdaQueryWrapper<>();
        Wrapper2.eq(TaskView::getName,"declare")
                .eq(TaskView::getAssignee,obj.getString("assessorId"));
        List<TaskView> task2=TaskViewMapper.selectList(Wrapper2);
        List<EmpPositionView> empList=new ArrayList<>();
        task2.forEach(x->{
            LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(EmployeePosition::getProcessDefinitionId,x.getProcInstId());
            EmployeePosition one=EmployeePositionMapper.selectOne(queryWrapper);

            LambdaQueryWrapper<EmpPositionView> query=new LambdaQueryWrapper<>();
            query.eq(EmpPositionView::getEmpId,one.getEmpId())
                    .eq(EmpPositionView::getPositionId,one.getPositionId());
            empList.add(EmpPositionViewMapper.selectOne(query));
        });
        Integer count2 = empList.size();

        LambdaQueryWrapper<TaskView> Wrapper3=new LambdaQueryWrapper<>();
        Wrapper3.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .and(qw -> qw.eq(TaskView::getName, "back_wageEmp_piece")
                        .or().eq(TaskView::getName, "back_wageEmp_kpi"))
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task3=TaskViewMapper.selectList(Wrapper3);
        List<BackWait> backWaitList3=new ArrayList<>();
        task3.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList3.add(BackWaitMapper.selectOne(queryWrapper));
        });
        Integer count3 = backWaitList3.size();

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("count1", count1);
        resultMap.put("count2", count2);
        resultMap.put("count3", count3);

        return R.success(resultMap);
    }


    @PostMapping("/thirdTaskCount")
    private R<Map<String, Integer>> getThirdCount(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> Wrapper1=new LambdaQueryWrapper<>();
        Wrapper1.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getState,"ACTIVE")
                .and(qw -> qw.eq(TaskView::getName, "piece")
                        .or().eq(TaskView::getName, "kpi"));
        List<TaskView> task1=TaskViewMapper.selectList(Wrapper1);
        List<TaskView> taskList1 = task1.stream()
                .collect(Collectors.toMap(TaskView::getEmpName, Function.identity(), (oldValue, newValue) -> oldValue))
                .values()
                .stream()
                .distinct()
                .collect(Collectors.toList());
        Integer count1 = taskList1.size();

        LambdaQueryWrapper<TaskView> Wrapper2=new LambdaQueryWrapper<>();
        Wrapper2.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"third")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task2=TaskViewMapper.selectList(Wrapper2);
        Integer count2 = task2.size();

        LambdaQueryWrapper<TaskView> Wrapper3=new LambdaQueryWrapper<>();
        Wrapper3.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getState,"ACTIVE")
                .and(qw -> qw.eq(TaskView::getName, "score")
                        .or().eq(TaskView::getName, "okr"));
        List<TaskView> task3=TaskViewMapper.selectList(Wrapper2);
        List<TaskView> taskList3 = task3.stream()
                .collect(Collectors.toMap(TaskView::getEmpName, Function.identity(), (oldValue, newValue) -> oldValue))
                .values()
                .stream()
                .distinct()
                .collect(Collectors.toList());
        Integer count3 = taskList3.size();

        LambdaQueryWrapper<TaskView> Wrapper4=new LambdaQueryWrapper<>();
        Wrapper4.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .and(qw -> qw.eq(TaskView::getName, "back_score")
                        .or().eq(TaskView::getName, "back_okr"))
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task4=TaskViewMapper.selectList(Wrapper4);
        List<BackWait> backWaitList4=new ArrayList<>();
        task4.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList4.add(BackWaitMapper.selectOne(queryWrapper));
        });
        Integer count4 = backWaitList4.size();

        LambdaQueryWrapper<TaskView> Wrapper5=new LambdaQueryWrapper<>();
        Wrapper5.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .and(qw -> qw.eq(TaskView::getName, "back_fourth_piece")
                        .or().eq(TaskView::getName, "back_fourth_kpi"))
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task5=TaskViewMapper.selectList(Wrapper5);
        List<BackWait> backWaitList5=new ArrayList<>();
        task5.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList5.add(BackWaitMapper.selectOne(queryWrapper));
        });
        Integer count5=backWaitList5.size();

        LambdaQueryWrapper<TaskView> Wrapper6=new LambdaQueryWrapper<>();
        Wrapper6.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .and(qw -> qw.eq(TaskView::getName, "back_third_piece")
                        .or().eq(TaskView::getName, "back_third_kpi")
                        .or().eq(TaskView::getName, "back_third_okr")
                        .or().eq(TaskView::getName, "back_third_score"))
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task6=TaskViewMapper.selectList(Wrapper6);
        List<BackWait> backWaitList6=new ArrayList<>();
        task6.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList6.add(BackWaitMapper.selectOne(queryWrapper));
        });
        Integer count6=backWaitList6.size();

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("count1", count1);
        resultMap.put("count2", count2);
        resultMap.put("count3", count3);
        resultMap.put("count4", count4);
        resultMap.put("count5", count5);
        resultMap.put("count6", count6);

        return R.success(resultMap);
    }


    @PostMapping("/secondTaskCount")
    private R<Map<String, Integer>> getSecondCount(@RequestBody JSONObject obj){
        LambdaQueryWrapper<TaskView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"second")
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task= TaskViewMapper.selectList(queryWrapper);

        List<Long> positionList=new ArrayList<>();
        task.forEach(x->{
            LambdaQueryWrapper<EmployeePosition> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(EmployeePosition::getEmpId,x.getStartUserId())
                    .eq(EmployeePosition::getProcessDefinitionId,x.getProcInstId());
            positionList.add(EmployeePositionMapper.selectOne(queryWrapper1).getPositionId());
        });

        Map<Long, Integer> countMap = new HashMap<>();
        for (Long num : positionList) {
            countMap.put(num, countMap.getOrDefault(num, 0) + 1);
        }

        List<PositionView> positionViews= PositionViewService.list();
        List<Long> allPosition=new ArrayList<>();
        positionViews.forEach(x->{
            allPosition.add(x.getId());
        });
        Map<Long, Integer> count = new HashMap<>();
        for (Long num : allPosition) {
            count.put(num, count.getOrDefault(num, 0) + 1);
        }

        List<Long> completeList=new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : countMap.entrySet()) {
            Long key = entry.getKey();
            Integer value1 = entry.getValue();
            if (count.containsKey(key) && count.get(key).equals(value1)) {
                completeList.add(key);
            }
        }
        Integer count1 = completeList.size();

        LambdaQueryWrapper<TaskView> Wrapper2=new LambdaQueryWrapper<>();
        Wrapper2.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getState,"ACTIVE")
                .and(qw -> qw.eq(TaskView::getName, "score")
                        .or().eq(TaskView::getName, "okr"));
        List<TaskView> task2=TaskViewMapper.selectList(Wrapper2);
        List<TaskView> taskList2 = task2.stream()
                .collect(Collectors.toMap(TaskView::getEmpName, Function.identity(), (oldValue, newValue) -> oldValue))
                .values()
                .stream()
                .distinct()
                .collect(Collectors.toList());
        Integer count2 = taskList2.size();

        LambdaQueryWrapper<TaskView> Wrapper3=new LambdaQueryWrapper<>();
        Wrapper3.eq(TaskView::getAssignee,obj.getString("assessorId"))
                .and(qw -> qw.eq(TaskView::getName, "back_score")
                        .or().eq(TaskView::getName, "back_okr"))
                .eq(TaskView::getState,"ACTIVE");
        List<TaskView> task3=TaskViewMapper.selectList(Wrapper3);
        List<BackWait> backWaitList3=new ArrayList<>();
        task3.forEach(x->{
            LambdaQueryWrapper<BackWait> queryWrapper3=new LambdaQueryWrapper<>();
            queryWrapper3.eq(BackWait::getEmpId,x.getStartUserId())
                    .eq(BackWait::getProcessDefineId,x.getProcInstId());
            backWaitList3.add(BackWaitMapper.selectOne(queryWrapper3));
        });
        Integer count3 = backWaitList3.size();

        Integer count4=okr(obj)+piece(obj)+score(obj)+kpi(obj);

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("count1", count1);
        resultMap.put("count2", count2);
        resultMap.put("count3", count3);
        resultMap.put("count4", count4);

        return R.success(resultMap);
    }

    private Integer okr(JSONObject obj){
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

        return samePositionIds.size();
    }

    private Integer piece(JSONObject obj){
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

        return samePositionIds.size();
    }

    private Integer score(JSONObject obj){
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
        return samePositionIds.size();
    }

    private Integer kpi(JSONObject obj){
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
        return samePositionIds.size();
    }

}
