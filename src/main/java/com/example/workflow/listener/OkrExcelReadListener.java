package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.model.entity.EmpPositionView;
import com.example.workflow.model.entity.Employee;
import com.example.workflow.model.entity.OkrKey;
import com.example.workflow.model.entity.OkrRule;
import com.example.workflow.model.feedback.ErrorExcelWrite;
import com.example.workflow.model.feedback.OkrError;
import com.example.workflow.model.pojo.OkrExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OkrExcelReadListener implements ReadListener<OkrExcel> {
    private static final int BATCH_COUNT = 20;
    private List<OkrExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    public OkrExcelReadListener() {

    }
    @Override
    public void invoke(OkrExcel data, AnalysisContext context) {
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
            List<OkrError> errorList=new ArrayList<>();
            cachedDataList.stream().filter(Objects::nonNull).forEach(okrExcel -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM");
                System.out.println(okrExcel.getYearMonth());
                YearMonth yearMonth = YearMonth.parse(okrExcel.getYearMonth(), formatter);
                LocalDateTime updateTime = yearMonth.atDay(1).atStartOfDay();
                LocalDateTime createTime = yearMonth.atDay(1).atStartOfDay();

                LocalDateTime beginTime = yearMonth.atDay(1).atStartOfDay();
                LocalDateTime endTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

                OkrError error=new OkrError();
                if(okrExcel.getTarget().isEmpty()){
                    BeanUtils.copyProperties(okrExcel, error);
                    error.setError("命名不得为空");
                    errorList.add(error);
                    return;
                }

                OkrRule okrRule= Db.lambdaQuery(OkrRule .class)
                        .eq(OkrRule::getTarget,okrExcel.getTarget())
                        .apply(StringUtils.checkValNotNull(beginTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                        .apply(StringUtils.checkValNotNull(endTime),
                                "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                        .one();

                Employee assessor=Db.lambdaQuery(Employee.class)
                        .eq(Employee::getNum,okrExcel.getAssessorNum())
                        .eq(Employee::getState,1)
                        .one();
                if(assessor==null){
                    BeanUtils.copyProperties(okrExcel, error);
                    error.setError("评分人不存在");
                    errorList.add(error);
                    return;
                }

                if(okrExcel.getOscore()==null){
                    BeanUtils.copyProperties(okrExcel, error);
                    error.setError("O分值不得为空");
                    errorList.add(error);
                    return;
                }

                EmpPositionView emp=Db.lambdaQuery(EmpPositionView.class)
                        .eq(EmpPositionView::getPosition,okrExcel.getPosition())
                        .eq(EmpPositionView::getDeptName,okrExcel.getDept())
                        .eq(EmpPositionView::getEmpName,okrExcel.getEmpName())
                        .eq(EmpPositionView::getState,1)
                        .one();
                if(emp==null){
                    BeanUtils.copyProperties(okrExcel, error);
                    error.setError("责任人不存在/责任人不在该岗位下/该岗位不在该部门下");
                    errorList.add(error);
                    return;
                }

                if(okrExcel.getKeyResult()==null){
                    BeanUtils.copyProperties(okrExcel, error);
                    error.setError("子目标不得为空");
                    errorList.add(error);
                    return;
                }
                if(okrExcel.getKeyWeight()==null){
                    BeanUtils.copyProperties(okrExcel, error);
                    error.setError("KR权重不得为空");
                    errorList.add(error);
                    return;
                }

                if(okrRule==null){
                    okrRule=new OkrRule();
                    okrRule.setTarget(okrExcel.getTarget());
                    okrRule.setAssessorId(assessor.getId());
                    okrRule.setTotalScore(okrExcel.getOscore());
                    okrRule.setUpdateTime(updateTime);
                    okrRule.setCreateTime(createTime);
                    Db.save(okrRule);
                }
                
                if (Check.noNull(emp)) {
                    OkrKey okrKey = new OkrKey();;
                    okrKey.setRuleId(okrRule.getId());
                    okrKey.setKeyResult(okrExcel.getKeyResult());
                    okrKey.setKeyWeight(okrExcel.getKeyWeight());
                    okrKey.setPositionId(emp.getPositionId());
                    okrKey.setLiaEmpId(emp.getEmpId());
                    okrKey.setUpdateTime(createTime);
                    okrKey.setCreateTime(updateTime);

                    Db.save(okrKey);
                }
            });
            ErrorExcelWrite.setErrorCollection(errorList);
        }
    }
}
