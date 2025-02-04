package com.example.workflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.common.R;
import com.example.workflow.model.entity.Employee;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.Position;
import com.example.workflow.model.entity.PositionAssessor;
import com.example.workflow.model.vo.EmployeeVo;
import com.example.workflow.model.vo.PositionAssessorView;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.PositionAssessorViewService;
import com.example.workflow.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/assessor")
public class PositionAssessorController {
    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private PositionAssessorViewService PositionAssessorViewService;
    @Autowired
            private PositionService PositionService;
    @Autowired
            private EmployeePositionService EmployeePositionService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);


    @PostMapping("/add")
    private R add(@RequestBody PositionAssessor form){
        PositionAssessorService.save(form);
        return R.success();
    }

    @PostMapping("/update")
    private R update(@RequestBody PositionAssessor form){
        PositionAssessorService.updateById(form);
        return R.success();
    }


    @GetMapping("/page")
    private R<Page<PositionAssessorView>> list(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){

        Page<PositionAssessorView> pageInfo=new Page<PositionAssessorView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<PositionAssessorView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(PositionAssessorView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        Page<PositionAssessorView> list=PositionAssessorViewService.page(pageInfo,queryWrapper);

        return R.success(list);
    }


    @PostMapping("/copy")
    private R copy(){
        List<PositionAssessorView> list=PositionAssessorViewService.lambdaQuery()
                .eq(PositionAssessorView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .list();
        if(!list.isEmpty()){
            return R.error("本月审核人和审核时限已复制，请勿重复操作");
        }

        PositionAssessorService.monthCopy();
        return R.success();
    }


    @GetMapping("/assessorPastPage")
    public R<Page> assessorPastPage(@RequestParam("page") String page, @RequestParam("page_size") String pageSize){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);

        Page<PositionAssessorView> pageInfo=new Page<PositionAssessorView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<PositionAssessorView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.apply(StringUtils.checkValNotNull(beginTime),
                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime);
        PositionAssessorViewService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @GetMapping("/search")
    public R<Page> searchPage(@RequestParam("page") String page
            , @RequestParam("page_size") String pageSize
            ,@RequestParam(defaultValue = "") String position){
        Page<PositionAssessorView> pageInfo=new Page<PositionAssessorView>(Long.parseLong(page),Long.parseLong(pageSize));
        LambdaQueryWrapper<PositionAssessorView> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PositionAssessorView::getPosition)
                .like(PositionAssessorView::getPosition,position)
                .eq(PositionAssessorView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        PositionAssessorViewService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    @GetMapping("/ceo/{id}")
    private R ceo(@PathVariable Integer id){
        ArrayList<EmployeeVo> employeeVos = new ArrayList<>();
        List<Position> positions = PositionService.lambdaQuery()
                .eq(Position::getState, 1)
                .eq(Position::getType,id)
                .list();
                positions.stream().filter(Objects::nonNull).filter(position -> position.getType() < 5).forEach(position -> {
                    Long positionId = position.getId();
                    List<EmployeePosition> employeePositions = EmployeePositionService.lambdaQuery().eq(EmployeePosition::getPositionId, positionId).list();
                    employeePositions.stream().filter(Objects::nonNull).forEach(employeePosition -> {
                        Long empId = employeePosition.getEmpId();
                        Employee employee = Db.getById(empId, Employee.class);
                        if (employee != null) {
                            String name = employee.getName();
                            EmployeeVo employeeVo = new EmployeeVo();
                            employeeVo.setCeoLevel(id);
                            employeeVo.setName(name);
                            employeeVo.setId(empId);
                            employeeVos.add(employeeVo);
                        }
                    });
                });

            return R.success(employeeVos);

    }
}
