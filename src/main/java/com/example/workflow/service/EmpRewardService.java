package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.model.entity.EmpReward;
import com.example.workflow.model.pojo.EmployeeRewardExcel;

import java.util.Collection;

public interface EmpRewardService extends IService<EmpReward> {
    Collection<EmployeeRewardExcel> getAllEmployeeRewardExcel();
}
