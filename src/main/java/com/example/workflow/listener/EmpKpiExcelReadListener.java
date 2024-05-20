package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.common.R;
import com.example.workflow.entity.*;
import com.example.workflow.feedback.EmpKpiError;
import com.example.workflow.feedback.ErrorExcelWrite;
import com.example.workflow.pojo.EmpKpiExcel;
import com.example.workflow.pojo.EmpPieceExcel;
import com.example.workflow.service.EmpKpiService;
import com.example.workflow.utils.Check;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmpKpiExcelReadListener implements ReadListener<EmpKpiExcel> {
    private static final int BATCH_COUNT = 20;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    private EmpKpiService EmpKpiService;

    private List<EmpKpiExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    public EmpKpiExcelReadListener() {

    }

    @Override
    public void invoke(EmpKpiExcel data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();

            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
    }

    private void saveData() {
        List<EmpKpiError> errorList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(empKpiExcel -> {
                String num = empKpiExcel.getNum();
                Employee employee = Db.lambdaQuery(Employee.class)
                        .eq(Employee::getNum, num).one();

                EmpKpiError error=new EmpKpiError();
                if(employee==null){
                    BeanUtils.copyProperties(empKpiExcel, error);
                    error.setError("员工不存在");
                    errorList.add(error);
                    return;
                }

                Dept dept=Db.lambdaQuery(Dept.class)
                        .eq(Dept::getDeptName,empKpiExcel.getDept())
                        .eq(Dept::getState,1).one();
                if(dept==null){
                    BeanUtils.copyProperties(empKpiExcel, error);
                    error.setError("员工所属部门错误");
                    errorList.add(error);
                    return;
                }

                Position position = Db.lambdaQuery(Position.class)
                        .eq(Position::getPosition, empKpiExcel.getPositionName())
                        .eq(Position::getDeptId,dept.getId()).one();
                if(position==null){
                    BeanUtils.copyProperties(empKpiExcel, error);
                    error.setError("员工不在该岗位");
                    errorList.add(error);
                    return;
                }

                KpiRule kpi= Db.lambdaQuery(KpiRule.class).eq(KpiRule::getName,empKpiExcel.getKpiName())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one();
                if(kpi==null){
                    BeanUtils.copyProperties(empKpiExcel, error);
                    error.setError("该提成条目不存在");
                    errorList.add(error);
                    return;
                }

                if (Check.noNull(empKpiExcel.getInTarget1(),
                        empKpiExcel.getInTarget2(),
                        empKpiExcel.getType(),employee,num,dept,position,kpi.getId())) {
                    EmpKpi empKpi = new EmpKpi();;
                    empKpi.setEmpId(employee.getId());
                    empKpi.setKpiId(kpi.getId());
                    empKpi.setInTarget2(empKpiExcel.getInTarget2());
                    empKpi.setInTarget1(empKpiExcel.getInTarget1());
                    if(empKpiExcel.getType()==1)
                        empKpi.setResult(one(kpi.getId(),empKpiExcel.getInTarget2(),empKpiExcel.getInTarget1()));
                    else if(empKpiExcel.getType()==2)
                        empKpi.setResult(two(kpi.getId(),empKpiExcel.getInTarget2(),empKpiExcel.getInTarget1()));
                    Db.save(empKpi);
                }
            });
        }
        ErrorExcelWrite.setErrorCollection(errorList);
    }

    private BigDecimal one(Long kpiRuleId,BigDecimal inTarget1,BigDecimal inTarget2){
        List<KpiPercent> list= Db.lambdaQuery(KpiPercent.class).orderByAsc(KpiPercent::getKpiKey)
                .eq(KpiPercent::getKpiId,kpiRuleId)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();
        String rules=EmpKpiService.defineRule1(list,inTarget2);

        KieHelper helper = new KieHelper();
        helper.addContent(rules, ResourceType.DRL);
        KieSession kSession = helper.build().newKieSession();

        Order3 order = new Order3();
        order.setInTarget1(inTarget1);
        order.setInTarget2(inTarget2);

        kSession.insert(order);
        kSession.fireAllRules();
        kSession.dispose();

        return BigDecimal.valueOf(order.getOutNum());
    }

    private BigDecimal two(Long kpiRuleId,BigDecimal inTarget1,BigDecimal inTarget2){
        KpiPercent kpiPercent= Db.lambdaQuery(KpiPercent.class)
                .eq(KpiPercent::getKpiId,kpiRuleId)
                .eq(KpiPercent::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();

        return EmpKpiService.defineRule2(kpiPercent,inTarget1,inTarget2);
    }
}
