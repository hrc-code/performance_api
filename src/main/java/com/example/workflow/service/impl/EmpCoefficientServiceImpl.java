package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.EmpCoefficient;
import com.example.workflow.entity.RegionCoefficient;
import com.example.workflow.mapper.EmpCoefficientMapper;
import com.example.workflow.service.EmpCoefficientService;
import com.example.workflow.service.RegionCoefficientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class EmpCoefficientServiceImpl extends ServiceImpl<EmpCoefficientMapper, EmpCoefficient> implements EmpCoefficientService {
    @Autowired
    private EmpCoefficientService EmpCoefficientService;
    @Autowired
    private RegionCoefficientService RegionCoefficientService;
    @Override
    public void monthCopy(){

        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()).minusMonths(1), LocalTime.MAX);

        List<EmpCoefficient> list=EmpCoefficientService.lambdaQuery()
                .eq(EmpCoefficient::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        list.forEach(x->{
            x.setId(null);

            RegionCoefficient last= RegionCoefficientService.lambdaQuery()
                    .eq(RegionCoefficient::getId,x.getRegionCoefficientId())
                            .one();

            LocalDateTime begin = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);
            RegionCoefficient regionCoefficient=RegionCoefficientService.lambdaQuery()
                            .eq(RegionCoefficient::getRegion,last.getRegion())
                    .apply(StringUtils.checkValNotNull(begin),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", begin)
                    .apply(StringUtils.checkValNotNull(end),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", end)
                            .one();

            x.setRegionCoefficientId(regionCoefficient.getId());
            EmpCoefficientService.save(x);
        });
    }
}
