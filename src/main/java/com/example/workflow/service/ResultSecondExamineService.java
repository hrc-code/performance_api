package com.example.workflow.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.ResultSecondExamine;

public interface ResultSecondExamineService extends IService<ResultSecondExamine> {

    void rePieceSecondFifth(JSONObject form);

    void reKpiSecondFifth(JSONObject form);

    void reScoreSecondFifth(JSONObject form);

    void reOkrSecondFifth(JSONObject form, EmployeePosition emp);

    void rePieceSecondFourth(JSONObject form);

    void reKpiSecondFourth(JSONObject form);

    void reScoreSecondFourth(JSONObject form);

    void reOkrSecondFourth(JSONObject form, EmployeePosition emp);

    void rePieceSecondThird(JSONObject form);

    void reKpiSecondThird(JSONObject form);
}
