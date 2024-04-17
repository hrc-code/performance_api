package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.workflow.entity.PieceRule;
import com.example.workflow.entity.PositionPiece;
import com.example.workflow.mapper.PieceRuleMapper;
import com.example.workflow.service.PieceRuleService;
import com.example.workflow.service.PositionPieceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
public class PieceRuleServiceImpl extends ServiceImpl<PieceRuleMapper, PieceRule> implements PieceRuleService {

    @Autowired
    private PieceRuleService PieceRuleService;
    @Autowired
    private PositionPieceService PositionPieceService;


    @Override
    public void monthCopy(){
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1).minusMonths(1), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()).minusMonths(1), LocalTime.MAX);

        List<PieceRule> list=PieceRuleService.lambdaQuery()
                .eq(PieceRule::getState,1)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        list.forEach(x->{
            List<PositionPiece> positionPieces= PositionPieceService.lambdaQuery()
                    .eq(PositionPiece::getPieceId,x.getId())
                    .apply(StringUtils.checkValNotNull(beginTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                    .apply(StringUtils.checkValNotNull(endTime),
                            "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                    .list();

            x.setId(null);
            PieceRuleService.save(x);

            positionPieces.forEach(y->{
                y.setId(null);
                y.setPieceId(x.getId());
                PositionPieceService.save(y);
            });
        });
    }
}
