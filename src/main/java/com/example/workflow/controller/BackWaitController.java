package com.example.workflow.controller;


import com.example.workflow.common.R;
import com.example.workflow.entity.BackWait;
import com.example.workflow.service.BackWaitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/BackWait")
public class BackWaitController {
    @Autowired
    private BackWaitService BackWaitService;

    @GetMapping("/getOpinion")
    private R<BackWait> getOpinion(@RequestParam("empId") Long empId
            ,@RequestParam("positionId") Long positionId
            ,@RequestParam("type") String type){

        BackWait backWait=new BackWait();
        if(type.equals("score")){
            backWait= BackWaitService.lambdaQuery()
                    .eq(BackWait::getEmpId,empId)
                    .eq(BackWait::getPositionId,positionId)
                    .and(qw -> qw.eq(BackWait::getType, "fourth_score_back")
                            .or().eq(BackWait::getType, "third_score_back")
                            .or().eq(BackWait::getType, "second_score_back"))
                    .one();
        }
        else if(type.equals("piece")){
            backWait= BackWaitService.lambdaQuery()
                    .eq(BackWait::getEmpId,empId)
                    .eq(BackWait::getPositionId,positionId)
                    .and(qw -> qw.eq(BackWait::getType, "fourth_piece_back")
                            .or().eq(BackWait::getType, "third_piece_back")
                            .or().eq(BackWait::getType, "second_piece_back"))
                    .one();
        }
        else if(type.equals("okr")){
            backWait= BackWaitService.lambdaQuery()
                    .eq(BackWait::getEmpId,empId)
                    .eq(BackWait::getPositionId,positionId)
                    .and(qw -> qw.eq(BackWait::getType, "fourth_okr_back")
                            .or().eq(BackWait::getType, "third_okr_back")
                            .or().eq(BackWait::getType, "second_okr_back"))
                    .one();
        }
        else if(type.equals("kpi")){
            backWait= BackWaitService.lambdaQuery()
                    .eq(BackWait::getEmpId,empId)
                    .eq(BackWait::getPositionId,positionId)
                    .and(qw -> qw.eq(BackWait::getType, "fourth_kpi_back")
                            .or().eq(BackWait::getType, "third_kpi_back")
                            .or().eq(BackWait::getType, "second_kpi_back"))
                    .one();
        }

        return R.success(backWait);
    }
}
