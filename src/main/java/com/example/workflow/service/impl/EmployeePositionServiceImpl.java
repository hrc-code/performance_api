package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.EmployeePositionMapper;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.PositionForm;
import com.example.workflow.service.EmployeePositionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeePositionServiceImpl extends ServiceImpl<EmployeePositionMapper, EmployeePosition> implements EmployeePositionService {
    @Override
    public List<EmployeePosition> splitForm(PositionForm form, Long id){
        List<EmployeePosition> list=new ArrayList<>();
        /*for(Long i:form.getPersonList()){
            EmployeePosition one=new EmployeePosition();
            one.setEmpId(i);
            one.setPositionId(id);
            one.setIns(form.getIns());
            list.add(one);
        }*/
        return list;
    }
}
