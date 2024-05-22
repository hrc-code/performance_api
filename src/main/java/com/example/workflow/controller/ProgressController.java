package com.example.workflow.controller;

import cn.hutool.http.HttpInputStream;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.EmpPositionView;
import com.example.workflow.entity.TaskView;
import com.example.workflow.service.EmpPositionViewService;
import com.example.workflow.service.HistoryTaskService;
import com.example.workflow.service.TaskViewService;
import com.example.workflow.vo.HistoryTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/Progress")
public class ProgressController {
    @Autowired
    private EmpPositionViewService EmpPositionViewService;
    @Autowired
    private TaskViewService TaskViewService;
    @Autowired
    private HistoryTaskService HistoryTaskService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @PostMapping("/now")
    private R<List<Map<String, Object>>> getProgress(){
        List<EmpPositionView> empPositionViewList= EmpPositionViewService.lambdaQuery()
                .eq(EmpPositionView::getState,1)
                .eq(EmpPositionView::getAuditStatus,1)
                .list();

        List<Map<String, Object>> list=new ArrayList<>();
        empPositionViewList.forEach(x->{
            list.add(match(x));
        });

        return R.success(list);
    }

    @GetMapping("/search")
    private R<List<Map<String, Object>>> searchProgress(
            @RequestParam(defaultValue = "")  String empName
            , @RequestParam(defaultValue = "")  String dept
            ,@RequestParam(defaultValue = "") String position){

        List<EmpPositionView> empPositionViewList= EmpPositionViewService.lambdaQuery()
                .like(EmpPositionView::getPosition,position)
                .like(EmpPositionView::getEmpName,empName)
                .like(EmpPositionView::getDeptName,dept)
                .eq(EmpPositionView::getState,1)
                .eq(EmpPositionView::getAuditStatus,1)
                .list();

        List<Map<String, Object>> list=new ArrayList<>();
        empPositionViewList.forEach(x->{
            list.add(match(x));
        });

        return R.success(list);
    }

    public Map<String,Object> match(EmpPositionView x){
        Map<String, Object> data = new HashMap<>();
        List<TaskView> taskViewList= TaskViewService.lambdaQuery()
                .eq(TaskView::getProcInstId,x.getProcessDefinitionId())
                .eq(TaskView::getStartUserId,x.getEmpId())
                .eq(TaskView::getState,"ACTIVE")
                .list();

        boolean allContainDeclare = taskViewList.stream()
                .allMatch(taskView -> taskView.getName().contains("declare"));
        boolean allContainFourthEnter = taskViewList.stream()
                .allMatch(taskView -> taskView.getName().contains("score")||taskView.getName().contains("okr"));
        boolean allContainFourth = taskViewList.stream()
                .allMatch(taskView -> taskView.getName().contains("piece")||taskView.getName().contains("kpi"));
        boolean allContainThird = taskViewList.stream()
                .allMatch(taskView -> taskView.getName().contains("third"));
        boolean allContainSecond = taskViewList.stream()
                .allMatch(taskView -> taskView.getName().contains("second"));

        List<HistoryTask> historyTaskList= HistoryTaskService.lambdaQuery()
                .eq(HistoryTask::getProcInstId,x.getProcessDefinitionId())
                .eq(HistoryTask::getDeleteReason,"completed")
                .isNotNull(HistoryTask::getEmpName)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (start_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (start_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        String task1="";
        String task2="";
        String task3="";
        String task4="";
        String task5="";
        for(HistoryTask y:historyTaskList){
            if(y.getName().equals("declare")){
                task1=task1.concat("["+y.getAssessorName()+"-"+y.getEndTime()+"-完成任务]");
            }
            else if(y.getName().equals("score")||y.getName().equals("okr")){
                task2=task2.concat("["+y.getAssessorName()+"-"+y.getEndTime()+"-完成任务]");
            }
            else if(y.getName().equals("kpi")||y.getName().equals("piece")){
                task3=task3.concat("["+y.getAssessorName()+"-"+y.getEndTime()+"-完成任务]");
            }
            else if(y.getName().equals("third")){
                task4=task4.concat("["+y.getAssessorName()+"-"+y.getEndTime()+"-完成任务]");
            }
            else if(y.getName().equals("second")){
                task5=task5.concat("["+y.getAssessorName()+"-"+y.getEndTime()+"-完成任务]");
            }
        }

        for(TaskView y:taskViewList){
            if(y.getName().equals("declare")){
                task1=task1.concat("["+y.getAssessorName()+"-"+y.getCreateTime()+"-未完成]");
            }
            else if(y.getName().equals("score")||y.getName().equals("okr")){
                task2=task2.concat("["+y.getAssessorName()+"-"+y.getCreateTime()+"-未完成]");
            }
            else if(y.getName().equals("kpi")||y.getName().equals("piece")){
                task3=task3.concat("["+y.getAssessorName()+"-"+y.getCreateTime()+"-未完成]");
            }
            else if(y.getName().equals("third")){
                task4=task4.concat("["+y.getAssessorName()+"-"+y.getCreateTime()+"-未完成]");
            }
            else if(y.getName().equals("second")){
                task5=task5.concat("["+y.getAssessorName()+"-"+y.getCreateTime()+"-未完成]");
            }
        }

        if(x.getType()==5){
            String finalTask1 = task1;
            String finalTask2 = task2;
            String finalTask3 = task3;
            String finalTask4 = task4;
            String finalTask5 = task5;
            List<Map<String, String>> items = Arrays.asList(
                    new HashMap<String, String>() {{
                        put("title", "绩效专员申报");
                        put("description", finalTask1);
                    }},
                    new HashMap<String, String>() {{
                        put("title", "评分/okr（没有则忽略）");
                        put("description", "\n"+finalTask2);
                    }},
                    new HashMap<String, String>() {{
                        put("title", "初审");
                        put("description", finalTask3);
                    }},
                    new HashMap<String, String>() {{
                        put("title", "复审");
                        put("description", finalTask4);
                    }},
                    new HashMap<String, String>() {{
                        put("title", "终审");
                        put("description", finalTask5);
                    }}
            );
            data.put("items", items);
            if(allContainDeclare)
                data.put("orderStatus", 0);
            else if(allContainFourthEnter)
                data.put("orderStatus", 1);
            else if(allContainFourth)
                data.put("orderStatus", 2);
            else if(allContainThird)
                data.put("orderStatus", 3);
            else if(allContainSecond)
                data.put("orderStatus", 4);
        }
        else if(x.getType()==4){
            String finalTask1 = task1;
            String finalTask2 = task2;
            String finalTask3 = task3;
            String finalTask5 = task5;
            List<Map<String, String>> items = Arrays.asList(
                    new HashMap<String, String>() {{
                        put("title", "绩效专员申报");
                        put("description", finalTask1);
                    }},
                    new HashMap<String, String>() {{
                        put("title", "评分/okr（没有则忽略）");
                        put("description", "\n"+finalTask2);
                    }},
                    new HashMap<String, String>() {{
                        put("title", "初审");
                        put("description", finalTask3);
                    }},
                    new HashMap<String, String>() {{
                        put("title", "终审");
                        put("description", finalTask5);
                    }}
            );
            data.put("items", items);

            if(allContainDeclare)
                data.put("orderStatus", 0);
            else if(allContainFourthEnter)
                data.put("orderStatus", 1);
            else if(allContainFourth)
                data.put("orderStatus", 2);
            else if(allContainSecond)
                data.put("orderStatus", 3);
        }
        else if(x.getType()==3){
            String finalTask1 = task1;
            String finalTask2 = task2;
            String finalTask5 = task5;
            List<Map<String, String>> items = Arrays.asList(
                    new HashMap<String, String>() {{
                        put("title", "绩效专员申报");
                        put("description", finalTask1);
                    }},
                    new HashMap<String, String>() {{
                        put("title", "评分/okr（没有则忽略）");
                        put("description", "\n"+finalTask2);
                    }},
                    new HashMap<String, String>() {{
                        put("title", "终审");
                        put("description", finalTask5);
                    }}
            );
            data.put("items", items);

            if(allContainDeclare)
                data.put("orderStatus", 0);
            else if(allContainFourthEnter)
                data.put("orderStatus", 1);
            else if(allContainSecond)
                data.put("orderStatus", 2);
        }
        data.put("empName",x.getEmpName());
        data.put("deptName",x.getDeptName());
        data.put("position",x.getPosition());

        return data;
    }
}
