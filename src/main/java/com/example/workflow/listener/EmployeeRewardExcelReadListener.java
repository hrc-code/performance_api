package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.model.entity.Dept;
import com.example.workflow.model.entity.EmpReward;
import com.example.workflow.model.entity.Employee;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.PdfFile;
import com.example.workflow.model.entity.Position;
import com.example.workflow.model.entity.Role;
import com.example.workflow.model.feedback.EmpRewardError;
import com.example.workflow.model.feedback.ErrorExcelWrite;
import com.example.workflow.model.pojo.EmployeeRewardExcel;
import com.example.workflow.utils.Check;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * 员工奖励 Excel 读取侦听器类*/
public class EmployeeRewardExcelReadListener implements ReadListener<EmployeeRewardExcel> {
    /**
     * 每隔10条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 20;

    /**
     * 缓存的数据
     */
    private List<EmployeeRewardExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    public EmployeeRewardExcelReadListener() {

    }

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(EmployeeRewardExcel data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
    }

    /** 向数据中保存数据*/
    private void saveData() {
        List<EmpRewardError> errorList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            cachedDataList.stream().filter(Objects::nonNull).forEach(employeeRewardExcel -> {
                // 通过num员工工号去员工表查询该员工的id
                String num = employeeRewardExcel.getNum();
                Employee employee = Db.lambdaQuery(Employee.class).eq(Employee::getNum, num).one();

                EmpRewardError error=new EmpRewardError();
                if(employee==null){
                    BeanUtils.copyProperties(employeeRewardExcel, error);
                    error.setError("员工不存在");
                    errorList.add(error);
                    return;
                }

                // 去角色表中把 name = 绩效专员 的 id查出来
                Role role = Db.lambdaQuery(Role.class).eq(Role::getRoleName, "绩效专员").one();

                Dept dept=Db.lambdaQuery(Dept.class)
                        .eq(Dept::getDeptName,employeeRewardExcel.getDept())
                        .eq(Dept::getState,1).one();
                if(dept==null){
                    BeanUtils.copyProperties(employeeRewardExcel, error);
                    error.setError("部门不存在");
                    errorList.add(error);
                    return;
                }

                // 通过岗位名去岗位表查询对应的岗位id
                Position position = Db.lambdaQuery(Position.class)
                        .eq(Position::getPosition, employeeRewardExcel.getPositionName())
                        .eq(Position::getDeptId,dept.getId()).one();
                if(position==null){
                    BeanUtils.copyProperties(employeeRewardExcel, error);
                    error.setError("该岗位不存在");
                    errorList.add(error);
                    return;
                }

                EmployeePosition employeePosition=Db.lambdaQuery(EmployeePosition.class)
                        .eq(EmployeePosition::getPositionId, position.getId())
                        .eq(EmployeePosition::getEmpId,employee.getId())
                        .one();
                if(employeePosition==null){
                    BeanUtils.copyProperties(employeeRewardExcel, error);
                    error.setError("该员工不在该岗位");
                    errorList.add(error);
                    return;
                }

                // 通过fileName 去pdf文件表查询 file_id
                String fileName = employeeRewardExcel.getFileName();
                PdfFile pdfFile = Db.lambdaQuery(PdfFile.class).eq(PdfFile::getFileName, fileName).one();
                if(pdfFile==null){
                    BeanUtils.copyProperties(employeeRewardExcel, error);
                    error.setError("该文件不存在");
                    errorList.add(error);
                    return;
                }

                if (Check.noNull(employee, role, pdfFile, position)) {
                    EmpReward empReward = new EmpReward();
                    String id = String.valueOf(pdfFile.getId());
                    Long fileId = Long.parseLong(id);
                    empReward.setEmpId(employee.getId());
                    empReward.setFileId(fileId);
                    empReward.setDeclareId(role.getId());
                    empReward.setPositionId(position.getId());
                    empReward.setReward(employeeRewardExcel.getReward());
                    Db.save(empReward);
                }
            });
        }
        ErrorExcelWrite.setErrorCollection(errorList);
    }
}
