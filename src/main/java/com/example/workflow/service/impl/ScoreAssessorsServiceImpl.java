package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.mapper.ScoreAssessorsMapper;
import com.example.workflow.model.entity.ScoreAssessors;
import com.example.workflow.model.entity.ScoreRuleForm;
import com.example.workflow.service.ScoreAssessorsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScoreAssessorsServiceImpl extends ServiceImpl<ScoreAssessorsMapper, ScoreAssessors> implements ScoreAssessorsService {

    @Override
    public List<ScoreAssessors> splitForm(ScoreRuleForm form,Long positionScoreId){
        List<ScoreAssessors> list=new ArrayList<>();
        for(ScoreAssessors i:form.getPersonList()){
            ScoreAssessors one=i;
            if(form.getId()==null){
                one.setPositionScoreId(positionScoreId);
            }
            else{
                one.setPositionScoreId(form.getId());
            }
            list.add(one);
        }
        return list;
    }

}
