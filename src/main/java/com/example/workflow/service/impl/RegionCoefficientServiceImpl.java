package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.RegionCoefficient;
import com.example.workflow.mapper.RegionCoefficientMapper;
import com.example.workflow.service.RegionCoefficientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class RegionCoefficientServiceImpl extends ServiceImpl<RegionCoefficientMapper, RegionCoefficient> implements RegionCoefficientService {
    @Autowired
    private RegionCoefficientService RegionCoefficientService;
    @Override
    public String defineRule(List<RegionCoefficient> list){
        String rule= "package resources.rules;\r\n";
        rule+="import com.example.workflow.entity.Order2;\r\n";
        for(RegionCoefficient i:list){
            rule=rule.concat("rule \"rule").concat(String.valueOf(i.getId())).concat("\"\r\n");
            rule += "when\r\n";
            rule=rule.concat( "\t $order2: Order2(inString ==\"").concat(String.valueOf(i.getRegion())).concat("\")");
            rule += "\r\nthen\r\n";
            rule=rule.concat("$order2.setOutNum(").concat(String.valueOf(i.getCoefficient())).concat(");\n");;
            rule += "end\r\n";
        }
        return rule;
    }

    /**
     * 复制上月
     */
    @Override
    public void monthCopy(){
        //上一个月
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()).minusMonths(1), LocalTime.MAX);

        List<RegionCoefficient> list=RegionCoefficientService.lambdaQuery()
                .eq(RegionCoefficient::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        list.forEach(x->{
            x.setId(null);
            RegionCoefficientService.save(x);
        });
    }
}
