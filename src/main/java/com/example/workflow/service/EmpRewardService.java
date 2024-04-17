package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.EmpReward;
import com.example.workflow.pojo.EmployeeRewardExcel;

import java.util.Collection;

public interface EmpRewardService extends IService<EmpReward> {
    Collection<EmployeeRewardExcel> getAllEmployeeRewardExcel();
}
