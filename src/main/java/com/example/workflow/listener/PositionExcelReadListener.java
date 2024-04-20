package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.*;
import com.example.workflow.pojo.EmployeeRewardExcel;
import com.example.workflow.pojo.PositionExcel;
import com.example.workflow.service.DeptService;
import com.example.workflow.utils.Check;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public class PositionExcelReadListener implements ReadListener<PositionExcel> {

    private static final int BATCH_COUNT = 20;
    private List<PositionExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    public PositionExcelReadListener() {

    }
    @Override
    public void invoke(PositionExcel data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();

            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
    }

    private void saveData() {
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(positionExcel -> {
                Long deptId= Db.lambdaQuery(Dept.class).eq(Dept::getDeptName,positionExcel.getDept())
                        .eq(Dept::getState,1).one().getId();

                Short type=0;
                if(positionExcel.getType().equals("二级CEO岗"))
                    type=2;
                else if(positionExcel.getType().equals("三级CEO岗"))
                    type=3;
                else if(positionExcel.getType().equals("四级CEO岗"))
                    type=4;
                else if(positionExcel.getType().equals("普通员工岗"))
                    type=5;

                Short kind=0;
                if(positionExcel.getKind().equals("无"))
                    kind=0;
                else if(positionExcel.getKind().equals("绩效与业绩挂钩"))
                    kind=1;
                else if(positionExcel.getKind().equals("绩效与服务挂钩"))
                    kind=2;
                else if(positionExcel.getKind().equals("绩效与工作量挂钩"))
                    kind=3;

                if (Check.noNull(deptId,type)) {
                    Position position = new Position();;
                    position.setPosition(positionExcel.getPosition());
                    position.setType(type);
                    position.setTypeName(positionExcel.getType());
                    position.setDeptId(deptId);
                    position.setKind(kind);
                    position.setKindName(positionExcel.getKind());
                    Db.save(position);
                }
            });
        }
    }
}
