package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.model.entity.Dept;
import com.example.workflow.model.entity.Position;
import com.example.workflow.model.entity.PositionAssessor;
import com.example.workflow.model.feedback.ErrorExcelWrite;
import com.example.workflow.model.feedback.PositionError;
import com.example.workflow.model.pojo.PositionExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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

            List<PositionError> errorList=new ArrayList<>();
            cachedDataList.stream().filter(Objects::nonNull).forEach(positionExcel -> {
                Dept dept= Db.lambdaQuery(Dept.class).eq(Dept::getDeptName,positionExcel.getDept())
                        .eq(Dept::getState,1).one();

                PositionError error=new PositionError();
                if(dept==null){
                    BeanUtils.copyProperties(positionExcel, error);
                    error.setError("部门不存在");
                    System.out.println(error);
                    System.out.println(positionExcel);
                    errorList.add(error);
                    return;
                }
                else if(!positionExcel.getType().equals("二级CEO岗")
                        &&!positionExcel.getType().equals("三级CEO岗")
                        &&!positionExcel.getType().equals("四级CEO岗")
                        &&!positionExcel.getType().equals("普通员工岗")){
                    BeanUtils.copyProperties(positionExcel, error);
                    error.setError("岗位类型不在选择范围");
                    errorList.add(error);
                    return;
                }
                else if(!positionExcel.getKind().equals("无")
                        &&!positionExcel.getKind().equals("绩效与业绩挂钩")
                        &&!positionExcel.getKind().equals("绩效与服务挂钩")
                        &&!positionExcel.getKind().equals("绩效与工作量挂钩")){
                    BeanUtils.copyProperties(positionExcel, error);
                    error.setError("绩效挂钩不在选择范围");
                    errorList.add(error);
                    return;
                }
                else if(positionExcel.getPosition().isEmpty()){
                    BeanUtils.copyProperties(positionExcel, error);
                    error.setError("岗位名称不得为空");
                    errorList.add(error);
                    return;
                }

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

                if (Check.noNull(dept.getId(),type,kind)) {
                    Position position = new Position();;
                    position.setPosition(positionExcel.getPosition());
                    position.setType(type);
                    position.setTypeName(positionExcel.getType());
                    position.setDeptId(dept.getId());
                    position.setKind(kind);
                    position.setKindName(positionExcel.getKind());
                    Db.save(position);

                    PositionAssessor positionAssessor=new PositionAssessor();
                    positionAssessor.setPositionId(position.getId());
                    positionAssessor.setFourthTimer("P1D");
                    positionAssessor.setSecondTimer("P1D");
                    positionAssessor.setThirdTimer("P1D");
                    Db.save(positionAssessor);
                }
            });
            ErrorExcelWrite.setErrorCollection(errorList);
        }
    }
}
