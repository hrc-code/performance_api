package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.EmpKpi;
import com.example.workflow.entity.EmpKpiView;
import com.example.workflow.entity.EmpWage;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.KpiPercent;
import com.example.workflow.mapper.EmpKpiMapper;
import com.example.workflow.mapper.EmpKpiViewMapper;
import com.example.workflow.service.EmpKpiService;
import com.example.workflow.service.EmpKpiViewService;
import com.example.workflow.service.EmpWageService;
import com.example.workflow.service.EmployeePositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EmpKpiServiceImpl extends ServiceImpl<EmpKpiMapper, EmpKpi> implements EmpKpiService {

    @Autowired
    private EmpKpiViewService EmpKpiViewService;
    @Autowired
            private EmpKpiViewMapper EmpKpiViewMapper;
    @Autowired
            private EmpWageService EmpWageService;
    @Autowired
            private EmployeePositionService EmployeePositionService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    @Override
    public String defineRule(List<KpiPercent> list, BigDecimal inTarget2){
        String rule= "package resources.rules;\r\n";
        rule+="import com.example.workflow.entity.Order3;\r\n";
        BigDecimal previous=new BigDecimal(0);
        for(KpiPercent i:list){
            BigDecimal t=i.getRulePercent().divide(new BigDecimal(100),2).multiply(inTarget2);
            BigDecimal result=i.getResultPercent().divide(new BigDecimal(100),2).multiply(inTarget2);
            if(i.getKpiKey()==1){
                rule=rule.concat("rule \"rule").concat(String.valueOf(i.getKpiKey())).concat("\"\r\n");
                rule += "when\r\n";
                rule=rule.concat( "\t $order3: Order3(inTarget1")
                        .concat(">=")
                        .concat("(")
                        .concat(String.valueOf(t))
                        .concat("))");
                rule += "\r\nthen\r\n";
                rule=rule.concat("$order3.setOutNum(")
                        .concat(String.valueOf(result))
                        .concat(");\n");;
                rule += "end\r\n";

                previous=i.getRulePercent();
            }
            else{
                BigDecimal r=previous.divide(new BigDecimal(100),2).multiply(i.getRulePercent());
                rule=rule.concat("rule \"rule").concat(String.valueOf(i.getKpiKey())).concat("\"\r\n");
                rule += "when\r\n";
                rule=rule.concat( "\t $order3: Order3(inTarget1 <(")
                        .concat(String.valueOf(r))
                        .concat(")")
                        .concat("&& inTarget1>=")
                        .concat("(")
                        .concat(String.valueOf(t))
                        .concat("))");
                rule += "\r\nthen\r\n";
                rule=rule.concat("$order3.setOutNum(")
                        .concat(String.valueOf(result))
                        .concat(");\r\n");;
                rule += "end\r\n";
            }
        }
        return rule;
    }

    @Override
    public void reChange(Long empKpiId, Long empId){
        EmpKpiView empKpiView= EmpKpiViewService.lambdaQuery()
                .eq(EmpKpiView::getEmpKpiId,empKpiId)
                .one();

        Long positionId=empKpiView.getPositionId();

        LambdaQueryWrapper<EmpKpiView> queryWrapper3=new LambdaQueryWrapper<>();
        queryWrapper3.eq(EmpKpiView::getPositionId,positionId)
                .eq(EmpKpiView::getEmpId,empId)
                .eq(EmpKpiView::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime);
        List<EmpKpiView> kpiList= EmpKpiViewMapper.selectList(queryWrapper3);

        AtomicReference<BigDecimal> kpiTotal = new AtomicReference<>(new BigDecimal(0));
        if(!kpiList.isEmpty()){
            kpiList.forEach(x->{
                if(x.getCorrectedValue()==null){
                    kpiTotal.updateAndGet(total -> total.add(x.getResult()));
                }
                else {
                    kpiTotal.updateAndGet(total -> total.add(x.getCorrectedValue()));
                }
            });
        }

        EmpWage empWage= EmpWageService.lambdaQuery()
                .eq(EmpWage::getEmpId,empId)
                .eq(EmpWage::getPositionId,positionId)
                .eq(EmpWage::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        empWage.setKpiWage(kpiTotal.get());

        LambdaQueryWrapper<EmployeePosition> queryWrapper5=new LambdaQueryWrapper<>();
        queryWrapper5.eq(EmployeePosition::getEmpId,empId)
                .eq(EmployeePosition::getPositionId,positionId)
                .eq(EmployeePosition::getState,1);
        EmployeePosition EmployeePosition= EmployeePositionService.getOne(queryWrapper5);

        BigDecimal result = empWage.getOkrWage()
                .add(empWage.getKpiWage())
                .add(empWage.getPieceWage())
                .add(empWage.getRewardWage())
                .add(empWage.getScoreWage())
                .multiply(EmployeePosition.getPosiPercent());
        empWage.setTotal(result);
        EmpWageService.updateById(empWage);
    }

    /*@Override
    public String defineRule(List<KpiPercent> list){
        String rule= "package resources.rules;\r\n";
        rule+="import com.example.workflow.entity.Order3;\r\n";
        BigDecimal previous=new BigDecimal(0);
        for(KpiPercent i:list){
            if(i.getKpiKey()==1){
                rule=rule.concat("rule \"rule").concat(String.valueOf(i.getId())).concat("\"\r\n");
                rule += "when\r\n";
                rule=rule.concat( "\t $order3: Order3(inNum >=").concat(String.valueOf(i.getRulePercent())).concat(")");
                rule += "\r\nthen\r\n";
                rule=rule.concat("$order3.setOutNum(").concat(String.valueOf(i.getResultPercent())).concat(");\n");;
                rule += "end\r\n";

                previous=i.getRulePercent();
            }
            else{
                rule=rule.concat("rule \"rule").concat(String.valueOf(i.getId())).concat("\"\r\n");
                rule += "when\r\n";
                rule=rule.concat( "\t $order3: Order3(inNum <").concat(String.valueOf(previous))
                        .concat("&& inNum>=").concat(String.valueOf(i.getRulePercent())).concat(")");
                rule += "\r\nthen\r\n";
                rule=rule.concat("$order3.setOutNum(").concat(String.valueOf(i.getResultPercent())).concat(");\r\n");;
                rule += "end\r\n";
            }
        }
        return rule;
    }*/
}
