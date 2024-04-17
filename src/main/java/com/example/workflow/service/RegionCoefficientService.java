package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.RegionCoefficient;

import java.util.List;

public interface RegionCoefficientService extends IService<RegionCoefficient> {

    String defineRule(List<RegionCoefficient> list);

    void monthCopy();
}
