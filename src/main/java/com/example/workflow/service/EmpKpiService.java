package com.example.workflow.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.EmpKpi;
import com.example.workflow.entity.KpiPercent;

import java.math.BigDecimal;
import java.util.List;

public interface EmpKpiService extends IService<EmpKpi> {
    String defineRule1(List<KpiPercent> list, BigDecimal inTarget2);

    BigDecimal defineRule2(KpiPercent kpiPercent, BigDecimal inTarget1, BigDecimal inTarget2);

    void reChange(Long empKpiId, Long empId);
}
