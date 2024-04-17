package com.example.workflow.controller;

import com.example.workflow.common.R;
import com.example.workflow.service.PositionAssessorService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    /*@RequestMapping("/test")
    public String test(){
        return "test";
    }*/
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private PositionAssessorService PositionAssessorService;

    @PostMapping("/test")
    private R test(){
        PositionAssessorService.monthCopy();
        return R.success();
    }

}
