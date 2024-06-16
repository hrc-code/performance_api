package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.mapper.EmpKpiViewMapper;
import com.example.workflow.mapper.EmpPieceViewMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.model.entity.BackWait;
import com.example.workflow.model.entity.EmpKpiView;
import com.example.workflow.model.entity.EmpPieceView;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.OkrKey;
import com.example.workflow.model.entity.OkrRule;
import com.example.workflow.model.entity.Position;
import com.example.workflow.model.entity.PositionAssessor;
import com.example.workflow.model.entity.PositionKpi;
import com.example.workflow.model.entity.PositionPiece;
import com.example.workflow.model.entity.PositionScore;
import com.example.workflow.model.entity.ScoreAssessors;
import com.example.workflow.model.entity.TaskState;
import com.example.workflow.model.entity.TaskView;
import com.example.workflow.service.BackWaitService;
import com.example.workflow.service.OkrKeyService;
import com.example.workflow.service.OkrRuleService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.PositionKpiSerivce;
import com.example.workflow.service.PositionPieceService;
import com.example.workflow.service.PositionScoreService;
import com.example.workflow.service.TaskViewService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/WageEmp")
public class WageEmpController {
    @Autowired
    private EmpKpiViewMapper EmpKpiViewMapper;
    @Autowired
    private EmpPieceViewMapper EmpPieceViewMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private com.example.workflow.service.EmployeePositionService EmployeePositionService;
    @Autowired
    private com.example.workflow.service.PositionService PositionService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private com.example.workflow.service.ScoreAssessorsService ScoreAssessorsService;
    @Autowired
    private PositionScoreService PositionScoreService;
    @Autowired
    private PositionPieceService PositionPieceService;
    @Autowired
    private PositionKpiSerivce PositionKpiSerivce ;
    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private OkrKeyService OkrKeyService;
    @Autowired
    private OkrRuleService OkrRuleService;
    @Autowired
            private BackWaitService BackWaitService;
    @Autowired
            private TaskViewService TaskViewService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);



    @PostMapping("/getState")
    private R<TaskState> getState(@RequestBody JSONObject obj){

        TaskState state=new TaskState();
        state.setKpiState(0);
        state.setPieceState(0);
        state.setOkrState(0);
        state.setScoreState(0);

        LambdaQueryWrapper<EmpKpiView> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(EmpKpiView::getEmpId,obj.getString("empId"))
                .eq(EmpKpiView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpKpiView> list1= EmpKpiViewMapper.selectList(queryWrapper1);
        if(!list1.isEmpty()) {
            state.setKpiState(1);
        }

        LambdaQueryWrapper<EmpPieceView> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmpPieceView::getEmpId,obj.getString("empId"))
                .eq(EmpPieceView::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpPieceView> list2= EmpPieceViewMapper.selectList(queryWrapper2);
        if(!list2.isEmpty()) {
            state.setPieceState(1);
        }

        if(state.getKpiState().equals(0)
                &&state.getOkrState().equals(0)
                &&state.getPieceState().equals(0)
                &&state.getScoreState().equals(0)){
            state.setTotalState(1);
        }
        else {
            state.setTotalState(0);
        }

        return R.success(state);
    }

    @PostMapping("/updateFlow")
    private R updateFlow(@RequestBody JSONObject obj){

        Map<String,Object> map = new HashMap<>();

        List<String> assessorList1 = new ArrayList<>();
            LambdaQueryWrapper<PositionScore> query1=new LambdaQueryWrapper<>();
            query1.eq(PositionScore::getPositionId,obj.getString("positionId"));
            List<PositionScore> num1= PositionScoreService.list(query1);

            num1.forEach(y->{
                List<ScoreAssessors> assessor= ScoreAssessorsService.lambdaQuery()
                        .eq(ScoreAssessors::getPositionScoreId,y.getId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')",endTime)
                        .list();

                assessor.forEach(z-> assessorList1.add(String.valueOf(z.getAssessorId())));
            });

        List<String> assessors1=assessorList1.stream().distinct().collect(Collectors.toList());
        if(assessors1.isEmpty()) {
            map.put("scoreAppoint", "false");
        } else{
            map.put("ASList", assessors1);
            map.put("scoreAppoint", "true");
        }

        List<PositionPiece> pieceList= PositionPieceService.lambdaQuery()
                .eq(PositionPiece::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')",endTime)
                .list();
        if(pieceList.isEmpty()) {
            map.put("pieceAppoint", "false");
        } else {
            map.put("pieceAppoint", "true");
        }

        List<PositionKpi> kpiList= PositionKpiSerivce.lambdaQuery()
                .eq(PositionKpi::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')",endTime)
                .list();
        if(kpiList.isEmpty()) {
            map.put("kpiAppoint", "false");
        } else {
            map.put("kpiAppoint", "true");
        }

        LocalDateTime lastBeginTime = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
        LocalDateTime latsEndTime = LocalDateTime.of(today.withDayOfMonth(1).minusDays(1), LocalTime.MAX);
        List<OkrKey> keyList=OkrKeyService.lambdaQuery()
                .eq(OkrKey::getLiaEmpId,obj.getString("empId"))
                .eq(OkrKey::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(lastBeginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", lastBeginTime)
                .apply(StringUtils.checkValNotNull(latsEndTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", latsEndTime)
                .list();
        List<String> assessorList2=new ArrayList<>();
        keyList.forEach(x->{
            OkrRule okrRule = OkrRuleService.lambdaQuery()
                    .eq(OkrRule::getId, x.getRuleId())
                    .one();
            if (Objects.nonNull(okrRule)) {
                assessorList2.add(okrRule.getAssessorId().toString());
            }
        });
        List<String> assessors2=assessorList2.stream().distinct().collect(Collectors.toList());
        if(assessors2.isEmpty()) {
            map.put("okrAppoint", "false");
        } else{
            map.put("AOList", assessors2);
            map.put("okrAppoint", "true");
        }

        Position position=PositionService.lambdaQuery()
                .eq(Position::getId,obj.getString("positionId"))
                .one();

        PositionAssessor assessor=PositionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId,obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        if(position.getType()==5){
            map.put("fourthAssessor",assessor.getFourthAssessorId().toString());
            map.put("fourthTimer",assessor.getFourthTimer());
            map.put("thirdAssessor",assessor.getThirdAssessorId().toString());
            map.put("thirdTimer",assessor.getThirdTimer());
        }
        else if(position.getType()==4){
            map.put("thirdAssessor",assessor.getThirdAssessorId().toString());
            map.put("thirdTimer",assessor.getThirdTimer());
            map.put("secondAssessor",assessor.getSecondAssessorId().toString());
            map.put("secondTimer",assessor.getSecondTimer());
        }
        else if(position.getType()==3){
            map.put("secondAssessor",assessor.getSecondAssessorId().toString());
            map.put("secondTimer",assessor.getSecondTimer());
        }

        LambdaQueryWrapper<EmployeePosition> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(EmployeePosition::getEmpId,obj.getString("empId"))
                .eq(EmployeePosition::getPositionId,obj.getString("positionId"));
        EmployeePosition EmployeePosition=EmployeePositionService.getOne(wrapper);

        LambdaQueryWrapper<TaskView> query=new LambdaQueryWrapper<>();
        query.eq(TaskView::getStartUserId,obj.getString("empId"))
                .eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId());
        TaskView task=TaskViewMapper.selectOne(query);
        taskService.complete(task.getId(),map);

        return R.success();
    }

    @PostMapping("/updateFlowAll")
    private R updateFlowAll(@RequestBody List<EmployeePosition> list){
        if(list.isEmpty()) {
            return R.error("未选择申报对象，请选择申报对象");
        }

        list.forEach(x->{
            Map<String,Object> map = new HashMap<>();

            List<String> assessorList1 = new ArrayList<>();
            LambdaQueryWrapper<PositionScore> query1=new LambdaQueryWrapper<>();
            query1.eq(PositionScore::getPositionId,x.getPositionId());
            List<PositionScore> num1= PositionScoreService.list(query1);

            num1.forEach(y->{
                List<ScoreAssessors> assessor= ScoreAssessorsService.lambdaQuery()
                        .eq(ScoreAssessors::getPositionScoreId,y.getId())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')",endTime)
                        .list();
                assessor.forEach(z-> assessorList1.add(String.valueOf(z.getAssessorId())));
            });

            List<String> assessors1=assessorList1.stream().distinct().collect(Collectors.toList());
            if(assessors1.isEmpty()) {
                map.put("scoreAppoint", "false");
            } else{
                map.put("ASList", assessors1);
                map.put("scoreAppoint", "true");
            }

            List<PositionPiece> pieceList= PositionPieceService.lambdaQuery()
                    .eq(PositionPiece::getPositionId,x.getPositionId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')",endTime)
                    .list();
            if(pieceList.isEmpty()) {
                map.put("pieceAppoint", "false");
            } else {
                map.put("pieceAppoint", "true");
            }

            List<PositionKpi> kpiList= PositionKpiSerivce.lambdaQuery()
                    .eq(PositionKpi::getPositionId,x.getPositionId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')",endTime)
                    .list();
            if(kpiList.isEmpty()) {
                map.put("kpiAppoint", "false");
            } else {
                map.put("kpiAppoint", "true");
            }

            LocalDateTime lastBeginTime = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
            LocalDateTime latsEndTime = LocalDateTime.of(today.withDayOfMonth(1).minusDays(1), LocalTime.MAX);
            List<OkrKey> keyList=OkrKeyService.lambdaQuery()
                    .eq(OkrKey::getLiaEmpId,x.getEmpId())
                    .eq(OkrKey::getPositionId,x.getPositionId())
                    .apply(StringUtils.checkValNotNull(lastBeginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", lastBeginTime)
                    .apply(StringUtils.checkValNotNull(latsEndTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", latsEndTime)
                    .list();
            List<String> assessorList2=new ArrayList<>();
            keyList.forEach(y-> {
                OkrRule okrRule = OkrRuleService.lambdaQuery()
                        .eq(OkrRule::getId, y.getRuleId())
                        .one();
                assessorList2.add(okrRule.getAssessorId().toString());
            });
            List<String> assessors2=assessorList2.stream().distinct().collect(Collectors.toList());
            if(assessors2.isEmpty()) {
                map.put("okrAppoint", "false");
            } else{
                map.put("AOList", assessors2);
                map.put("okrAppoint", "true");
            }

            Position position=PositionService.lambdaQuery()
                    .eq(Position::getId,x.getPositionId())
                    .one();

            PositionAssessor assessor=PositionAssessorService.lambdaQuery()
                    .eq(PositionAssessor::getPositionId,x.getPositionId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .one();

            if(position.getType()==5){
                map.put("fourthAssessor",assessor.getFourthAssessorId().toString());
                map.put("fourthTimer",assessor.getFourthTimer());
            }
            else if(position.getType()==4){
                map.put("thirdAssessor",assessor.getThirdAssessorId().toString());
                map.put("thirdTimer",assessor.getThirdTimer());
            }
            else if(position.getType()==3){
                map.put("secondAssessor",assessor.getSecondAssessorId().toString());
                map.put("secondTimer",assessor.getSecondTimer());
            }

            LambdaQueryWrapper<EmployeePosition> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(EmployeePosition::getEmpId,x.getEmpId())
                    .eq(EmployeePosition::getPositionId,x.getPositionId());
            EmployeePosition EmployeePosition=EmployeePositionService.getOne(wrapper);

            LambdaQueryWrapper<TaskView> query=new LambdaQueryWrapper<>();
            query.eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,EmployeePosition.getProcessDefinitionId());
            TaskView task=TaskViewMapper.selectOne(query);
            taskService.complete(task.getId(),map);
        });

        return R.success();
    }

    @PostMapping("/reAddPiece")
    private R reAddPiece(@RequestBody JSONObject obj){
        BackWait backWait=BackWaitService.lambdaQuery()
                .eq(BackWait::getPositionId,obj.getString("positionId"))
                .eq(BackWait::getEmpId,obj.getString("empId"))
                .and(qw -> qw.eq(BackWait::getType,"fourth_piece_back")
                        .or().eq(BackWait::getType,"third_piece_back")
                        .or().eq(BackWait::getType,"back_second_piece"))
                .one();

        TaskView taskView=TaskViewService.lambdaQuery()
                .eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"back_wageEmp_piece")
                .eq(TaskView::getStartUserId,obj.getString("empId"))
                .eq(TaskView::getProcInstId,backWait.getProcessDefineId())
                .one();

        PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId, obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        Map<String, Object> map = new HashMap<>();
        if(Objects.equals(obj.getString("positionType"), "5")){
            map.put("Assessor",nextAssessor.getFourthAssessorId().toString());
        }
        else if(Objects.equals(obj.getString("positionType"), "4")){
            map.put("Assessor",nextAssessor.getThirdAssessorId().toString());
        }
        else if(Objects.equals(obj.getString("positionType"), "3")){
            map.put("Assessor",nextAssessor.getSecondAssessorId().toString());
        }
        taskService.complete(taskView.getId(), map);

        return R.success();
    }


    @PostMapping("/reAddKpi")
    private R reAddKpi(@RequestBody JSONObject obj){
        BackWait backWait=BackWaitService.lambdaQuery()
                .eq(BackWait::getPositionId,obj.getString("positionId"))
                .eq(BackWait::getEmpId,obj.getString("empId"))
                .and(qw -> qw.eq(BackWait::getType,"fourth_kpi_back")
                        .or().eq(BackWait::getType,"third_kpi_back")
                        .or().eq(BackWait::getType,"back_second_kpi"))
                .one();

        TaskView taskView=TaskViewService.lambdaQuery()
                .eq(TaskView::getAssignee,obj.getString("assessorId"))
                .eq(TaskView::getName,"back_wageEmp_kpi")
                .eq(TaskView::getStartUserId,obj.getString("empId"))
                .eq(TaskView::getProcInstId,backWait.getProcessDefineId())
                .one();

        PositionAssessor nextAssessor = PositionAssessorService.lambdaQuery()
                .eq(PositionAssessor::getPositionId, obj.getString("positionId"))
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        Map<String, Object> map = new HashMap<>();
        if(Objects.equals(obj.getString("positionType"), "5")){
            map.put("Assessor",nextAssessor.getFourthAssessorId().toString());
        }
        else if(Objects.equals(obj.getString("positionType"), "4")){
            map.put("Assessor",nextAssessor.getThirdAssessorId().toString());
        }
        else if(Objects.equals(obj.getString("positionType"), "3")){
            map.put("Assessor",nextAssessor.getSecondAssessorId().toString());
        }
        taskService.complete(taskView.getId(), map);

        return R.success();
    }
}
