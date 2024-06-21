package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workflow.common.R;
import com.example.workflow.listener.PositionExcelReadListener;
import com.example.workflow.mapper.EmployeePositionMapper;
import com.example.workflow.mapper.PositionMapper;
import com.example.workflow.mapper.TaskViewMapper;
import com.example.workflow.model.entity.Dept;
import com.example.workflow.model.entity.EmpReward;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.OkrKey;
import com.example.workflow.model.entity.Position;
import com.example.workflow.model.entity.PositionAssessor;
import com.example.workflow.model.entity.PositionForm;
import com.example.workflow.model.entity.PositionKpi;
import com.example.workflow.model.entity.PositionPiece;
import com.example.workflow.model.entity.PositionScore;
import com.example.workflow.model.entity.PositionView;
import com.example.workflow.model.entity.ScoreAssessors;
import com.example.workflow.model.entity.TaskView;
import com.example.workflow.model.feedback.ErrorExcelWrite;
import com.example.workflow.model.feedback.PositionError;
import com.example.workflow.model.pojo.PositionExcel;
import com.example.workflow.service.DeptService;
import com.example.workflow.service.EmpRewardService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.OkrKeyService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.PositionKpiSerivce;
import com.example.workflow.service.PositionPieceService;
import com.example.workflow.service.PositionScoreService;
import com.example.workflow.service.PositionService;
import com.example.workflow.service.PositionViewService;
import com.example.workflow.service.ScoreAssessorsService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/Position")
public class PositionController {
    @Autowired
    private PositionService PositionService;
    @Autowired
    private PositionMapper PositionMapper;
    @Autowired
    private EmployeePositionService EmployeePositionService;
    @Autowired
    private PositionViewService PositionViewService;
    @Autowired
    private PositionScoreService PositionScoreService;
    @Autowired
    private PositionPieceService PositionPieceService;
    @Autowired
    private PositionKpiSerivce PositionKpiSerivce;
    @Autowired
    private ScoreAssessorsService ScoreAssessorsService;
    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private TaskViewMapper TaskViewMapper;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private EmployeePositionMapper EmployeePositionMapper;
    @Autowired
    private DeptService DeptService;
    @Autowired
            private OkrKeyService OkrKeyService;
    @Autowired
            private EmpRewardService EmpRewardService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @PostMapping("/list")
    private R<List<Position>> list(){
        List<Position> list= PositionMapper.selectList(null);

        return R.success(list);
    }


    @PostMapping("/getOne")
    private R<Position> getOne(@RequestBody JSONObject obj){

        LambdaQueryWrapper<Position> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(Position::getId,obj.getString("positionId"));
        Position one= PositionMapper.selectOne(queryWrapper1);

        return R.success(one);
    }

    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        Page<PositionView> pageInfo=new Page<PositionView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<PositionView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PositionView::getPosition)
                .eq(PositionView::getState,1);
        PositionViewService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    @GetMapping("/search")
    public R<Page> searchPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String position
            ,@RequestParam(defaultValue = "") String dept
            ,@RequestParam(defaultValue = "") String auditStatus
    ){

        List<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(1);
        list.add(2);
        list.add(3);

        switch(auditStatus){
            case "未考核":	list.clear();list.add(0);break;
            case "考核中":	list.clear();list.add(1);break;
            case "暂停":	    list.clear();list.add(2);break;
            case "考核完成":	list.clear();list.add(3); break;
        }


        System.out.println("后auditStatus:"+auditStatus);

        Page<PositionView> pageInfo=new Page<PositionView>(Long.parseLong(page),Long.parseLong(pageSize));

        LambdaQueryWrapper<PositionView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PositionView::getPosition)
                .like(PositionView::getPosition,position)
                .like(PositionView::getDeptName,dept)
                .in(PositionView::getAuditStatus,list)
                .eq(PositionView::getState,1);
        PositionViewService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    @PostMapping("/add")
    private R add(@RequestBody PositionForm form){
        Position check=PositionService.lambdaQuery()
                .eq(Position::getPosition,form.getPosition())
                .eq(Position::getDeptId,form.getDeptId())
                .eq(Position::getState,1)
                .one();
        if(check!=null){
            return R.error("同一个部门里的岗位不能命名重复");
        }

        Position position=PositionService.splitForm(form);
        PositionMapper.insert(position);

        PositionAssessor positionAssessor=new PositionAssessor();
        positionAssessor.setPositionId(position.getId());
        PositionAssessorService.save(positionAssessor);

        return R.success();
    }


    @PostMapping("/deleteOne")
    private R deleteOne(@RequestBody Position form){
        PositionMapper.deleteById(form);

        LambdaQueryWrapper<PositionScore> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(PositionScore::getPositionId,form.getId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<PositionScore> list=PositionScoreService.list(queryWrapper1);
        list.forEach(x->{
            LambdaQueryWrapper<ScoreAssessors> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(ScoreAssessors::getPositionScoreId,x.getId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
            ScoreAssessorsService.remove(queryWrapper);
        });
        PositionScoreService.remove(queryWrapper1);

        LambdaQueryWrapper<PositionPiece> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(PositionPiece::getPositionId,form.getId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        PositionPieceService.remove(queryWrapper2);

        LambdaQueryWrapper<PositionKpi> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(PositionKpi::getPositionId,form.getId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        PositionKpiSerivce.remove(queryWrapper3);

        LambdaQueryWrapper<OkrKey> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(OkrKey::getPositionId,form.getId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        OkrKeyService.remove(queryWrapper5);

        LambdaQueryWrapper<EmpReward> queryWrapper6=new LambdaQueryWrapper<>();
        queryWrapper6.eq(EmpReward::getPositionId,form.getId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        EmpRewardService.remove(queryWrapper6);

        LambdaQueryWrapper<EmployeePosition> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeePosition::getPositionId,form.getId());
        List<EmployeePosition> empList=EmployeePositionMapper.selectList(queryWrapper);
        empList.forEach(x->{
            LambdaQueryWrapper<TaskView> Wrapper=new LambdaQueryWrapper<>();
            Wrapper.eq(TaskView::getStartUserId,x.getEmpId())
                    .eq(TaskView::getProcInstId,x.getProcessDefinitionId())
                    .eq(TaskView::getState,"ACTIVE");
            TaskView task=TaskViewMapper.selectOne(Wrapper);

            if(task!=null){
                runtimeService.deleteProcessInstance(task.getProcInstId(),"删除原因");
            }
        });
        EmployeePositionService.removeBatchByIds(empList);

        LambdaQueryWrapper<PositionAssessor> queryWrapper4=new LambdaQueryWrapper<>();
        queryWrapper4.eq(PositionAssessor::getPositionId,form.getId())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);;
        PositionAssessorService.remove(queryWrapper4);

        return R.success();
    }


    @PostMapping("/getInf")
    private R<Map<String, List<Object>>> getInf(@RequestBody JSONObject obj){

        LambdaQueryWrapper<Position> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.eq(Position::getId,obj.getString("positionId"));
        Position position= PositionMapper.selectOne(queryWrapper1);

        LambdaQueryWrapper<EmployeePosition> queryWrapper2=new LambdaQueryWrapper<>();
        queryWrapper2.eq(EmployeePosition::getPositionId,obj.getString("positionId"));
        List<EmployeePosition> empList=EmployeePositionService.list(queryWrapper2);

        List<Object> inf = new ArrayList<>();
        inf.add(position);
        List<Object> list = new ArrayList<>(empList);

        Map<String, List<Object>> resultMap = new HashMap<>();
        resultMap.put("position",inf);
        resultMap.put("empList",list);

        return R.success(resultMap);
    }


    @PostMapping("/update")
    private R update(@RequestBody Position form){
        if(form.getType()==2)
            form.setTypeName("二级CEO岗");
        else if(form.getType()==3)
            form.setTypeName("三级CEO岗");
        else if(form.getType()==4)
            form.setTypeName("四级CEO岗");
        else if(form.getType()==5)
            form.setTypeName("普通员工岗");

        if(form.getKind()==0)
            form.setKindName("无");
        else if(form.getKind()==1)
            form.setKindName("绩效与业绩挂钩");
        else if(form.getKind()==2)
            form.setKindName("绩效与服务挂钩");
        else if(form.getKind()==3)
            form.setKindName("绩效与工作量挂钩");

        PositionMapper.updateById(form);
        return R.success();

    }

    @PostMapping("/getOnePosition")
    private R<List<Position>> getOnePosition(@RequestBody JSONObject obj){
        List<EmployeePosition> employeePositions=EmployeePositionService
                .lambdaQuery()
                .eq(EmployeePosition::getEmpId,obj.getString("empId"))
                .list();

        List<Position> positionList=new ArrayList<>();
        employeePositions.forEach(x->{
            Position position=PositionService
                    .lambdaQuery()
                    .eq(Position::getId,x.getPositionId())
                    .eq(Position::getState,1)
                    .one();
            positionList.add(position);
        });

        return R.success(positionList);
    }

    @PostMapping("/updateState")
    private R updateState(){
        List<Position> positionList=PositionService.lambdaQuery()
                .eq(Position::getState,1)
                        .list();

        positionList.forEach(x->{
            x.setAuditStatus(Short.parseShort("0"));
            PositionService.updateById(x);
        });

        return R.success();
    }

    @PostMapping("/upload")
    public void uploadExcel(MultipartFile file, HttpServletResponse response) throws IOException {
        EasyExcel.read(file.getInputStream(), PositionExcel.class, new PositionExcelReadListener()).sheet().doRead();

        if(!ErrorExcelWrite.getErrorCollection().isEmpty()){
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");

            EasyExcel.write(response.getOutputStream(), PositionError.class).sheet("错误部分").doWrite(ErrorExcelWrite.getErrorCollection());
        }
        ErrorExcelWrite.clearErrorCollection();
    }

    @PostMapping("/getTree")
   private R<Map<String, Map<String, List<Position>>>> getTree() {
        List<Position> list=PositionService.lambdaQuery().list();
        Map<String, Map<String, List<Position>>> groupedByKind = list.stream()
                .collect(Collectors.groupingBy(Position::getKindName,
                        Collectors.groupingBy(Position::getPosition)));

        return R.success(groupedByKind);
    }

    @PostMapping("/getCheckTree")
    private R<List<Map<String, Object>>> getCheckTree() {
        List<Dept> list=DeptService.lambdaQuery().list();
        List<Map<String, Object>> tree=new ArrayList<>();
        list.forEach(x->{
            Map<String, Object> map = new HashMap<>();
            map.put("id", x.getId().toString());
            map.put("label", x.getDeptName());
            map.put("disabled",true);

            List<Position> positionList=PositionService.lambdaQuery()
                            .eq(Position::getDeptId,x.getId())
                                    .list();
            List<Map<String, Object>> position=new ArrayList<>();
            positionList.forEach(y->{
                Map<String, Object> positionMap = new HashMap<>();
                positionMap.put("id", y.getId().toString());
                positionMap.put("label",y.getPosition());
                position.add(positionMap);
            });
            map.put("children",position);

            tree.add(map);
        });

        return R.success(tree);
    }
}

