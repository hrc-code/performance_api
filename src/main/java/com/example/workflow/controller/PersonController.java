package com.example.workflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.EmpKpiView;
import com.example.workflow.entity.EmpOkrView;
import com.example.workflow.entity.EmpPieceView;
import com.example.workflow.entity.EmpPositionView;
import com.example.workflow.entity.EmpScoreView;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.TaskState;
import com.example.workflow.mapper.EmpKpiViewMapper;
import com.example.workflow.mapper.EmpOkrViewMapper;
import com.example.workflow.mapper.EmpPieceViewMapper;
import com.example.workflow.mapper.EmpScoreViewMapper;
import com.example.workflow.service.EmpPositionViewService;
import com.example.workflow.service.PositionService;
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

@Slf4j
@RestController
@RequestMapping("/Person")
public class PersonController {
    @Autowired
    private com.example.workflow.service.EmployeePositionService EmployeePositionService;
    @Autowired
    private EmpScoreViewMapper EmpScoreViewMapper;
    @Autowired
    private EmpOkrViewMapper EmpOkrViewMapper;
    @Autowired
    private EmpPieceViewMapper EmpPieceViewMapper;
    @Autowired
    private EmpKpiViewMapper EmpKpiViewMapper;
    @Autowired
            private EmpPositionViewService EmpPositionViewService;
    @Autowired
            private PositionService PositionService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @PostMapping("/performance")
    public R<Map<String, List<Object>>> page(@RequestBody JSONObject obj){
        Long empId=Long.valueOf(obj.getString("empId"));

        LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpPositionView::getEmpId,empId);
        List<EmpPositionView> positionList= EmpPositionViewService.list(queryWrapper);

        List<Map<String, List<Object>>> result=new ArrayList<>();
        positionList.forEach(x->{

            TaskState state=new TaskState();
            state.setKpiState(0);
            state.setPieceState(0);
            state.setOkrState(0);
            state.setScoreState(0);

            LambdaQueryWrapper<EmpScoreView> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(EmpScoreView::getEmpId,empId)
                    .eq(EmpScoreView::getPositionId,x.getPositionId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
            List<EmpScoreView> list1= EmpScoreViewMapper.selectList(queryWrapper1);
            if(!list1.isEmpty())
                state.setScoreState(1);

            LambdaQueryWrapper<EmpPieceView> queryWrapper2=new LambdaQueryWrapper<>();
            queryWrapper2.eq(EmpPieceView::getEmpId,empId)
                    .eq(EmpPieceView::getPositionId,x.getPositionId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
            List<EmpPieceView> list2= EmpPieceViewMapper.selectList(queryWrapper2);
            if(!list2.isEmpty())
                state.setPieceState(1);

            LambdaQueryWrapper<EmpKpiView> queryWrapper3=new LambdaQueryWrapper<>();
            queryWrapper3.eq(EmpKpiView::getEmpId,empId)
                    .eq(EmpKpiView::getPositionId,x.getPositionId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
            List<EmpKpiView> list3= EmpKpiViewMapper.selectList(queryWrapper3);
            if(!list3.isEmpty())
                state.setKpiState(1);

            LambdaQueryWrapper<EmpOkrView> queryWrapper4=new LambdaQueryWrapper<>();
            queryWrapper4.eq(EmpOkrView::getLiaEmpId,empId)
                    .eq(EmpOkrView::getPositionId,x.getPositionId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
            List<EmpOkrView> list4= EmpOkrViewMapper.selectList(queryWrapper4);
            if(!list4.isEmpty())
                state.setOkrState(1);

            List<Object> score = new ArrayList<>(list1);
            List<Object> piece = new ArrayList<>(list2);
            List<Object> kpi = new ArrayList<>(list3);
            List<Object> okr = new ArrayList<>(list4);
            List<Object> taskState = new ArrayList<>();
            taskState.add(state);
            List<Object> person=new ArrayList<>();
            person.add(x);

            Map<String, List<Object>> resultMap = new HashMap<>();
            resultMap.put("score", score);
            resultMap.put("piece", piece);
            resultMap.put("kpi", kpi);
            resultMap.put("okr", okr);
            resultMap.put("state", taskState);
            resultMap.put("person",person);

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
    }

    @PostMapping("/state")
    public R<Boolean> state(@RequestBody JSONObject obj){
        Long empId=Long.valueOf(obj.getString("empId"));

        LambdaQueryWrapper<EmpPositionView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmpPositionView::getEmpId,empId);
        List<EmpPositionView> positionList= EmpPositionViewService.list(queryWrapper);

        if(positionList.isEmpty()){
            return R.success(false);
        }
        else{
            for (EmpPositionView x: positionList) {
                Position queriedPosition = PositionService.lambdaQuery()
                        .eq(Position::getId, x.getPositionId())
                        .eq(Position::getState, 1)
                        .one();
                if (queriedPosition.getAuditStatus() == 1||queriedPosition.getAuditStatus() ==0) {
                    return R.success(false);
                }
            }

            return R.success(true);
        }
    }
}
