package com.example.workflow.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.BackWait;
import com.example.workflow.mapper.BackWaitMapper;
import com.example.workflow.service.BackWaitService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

@Service
public class BackWaitServiceImpl extends ServiceImpl<BackWaitMapper, BackWait> implements BackWaitService {

    @Override
    public BackWait splitForm(JSONObject form, ProcessInstance processInstance, String ProcessKey,String opinion,String type){
        BackWait one=new BackWait();
        one.setPositionId(Long.valueOf(form.getString("positionId")));
        one.setEmpId(Long.valueOf(form.getString("empId")));
        one.setOpinion(opinion);
        one.setType(type);
        one.setProcessKey(ProcessKey);
        one.setProcessDefineId(processInstance.getId());
        return one;
    }

    @Override
    public BackWait addOne(Long positionId,Long empId,String type, ProcessInstance processInstance, String ProcessKey,String opinion){
        BackWait one=new BackWait();
        one.setPositionId(positionId);
        one.setEmpId(empId);
        one.setType(type);
        one.setProcessKey(ProcessKey);
        one.setProcessDefineId(processInstance.getId());
        return one;
    }

}
