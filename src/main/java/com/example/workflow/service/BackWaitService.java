package com.example.workflow.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.BackWait;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public interface BackWaitService extends IService<BackWait> {

    BackWait splitForm(JSONObject form, ProcessInstance processInstance, String ProcessKey, String opinion, String type);

    BackWait addOne(Long positionId, Long empId, String type, ProcessInstance processInstance, String ProcessKey, String opinion);
}
