package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.PositionMapper;
import com.example.workflow.model.entity.Position;
import com.example.workflow.model.entity.PositionForm;
import com.example.workflow.service.PositionService;
import org.springframework.stereotype.Service;

@Service
public class PositionServiceImpl extends ServiceImpl<PositionMapper, Position> implements PositionService {
    @Override
    public Position splitForm(PositionForm form){
        Position position=new Position();
        position.setPosition(form.getPosition());
        position.setDeptId(form.getDeptId());
        position.setType(form.getPositionType().shortValue());
        position.setKind(form.getKind().shortValue());;

        if(form.getPositionType().equals(2))
            position.setTypeName("二级CEO岗");
        else if(form.getPositionType().equals(3))
            position.setTypeName("三级CEO岗");
        else if(form.getPositionType().equals(4))
            position.setTypeName("四级CEO岗");
        else if(form.getPositionType().equals(5))
            position.setTypeName("普通员工岗");

        if(form.getKind().equals(0))
            position.setKindName("无");
        else if(form.getKind().equals(1))
            position.setKindName("绩效与业绩挂钩");
        else if(form.getKind().equals(2))
            position.setKindName("绩效与服务挂钩");
        else if(form.getKind().equals(3))
            position.setKindName("绩效与工作量挂钩");

        position.setIns(form.getIns());
        position.setState(form.getState()? (short) 1 : (short) 0);
        return position;
    }
}
