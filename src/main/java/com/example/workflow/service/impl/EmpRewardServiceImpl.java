package com.example.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.EmpReward;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.PdfFile;
import com.example.workflow.entity.Position;
import com.example.workflow.mapper.EmpRewardMapper;
import com.example.workflow.pojo.EmployeeRewardExcel;
import com.example.workflow.service.EmpRewardService;
import com.example.workflow.utils.Check;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class EmpRewardServiceImpl extends ServiceImpl<EmpRewardMapper, EmpReward> implements EmpRewardService {
    @Override
    public Collection<EmployeeRewardExcel> getAllEmployeeRewardExcel() {
        ArrayList<EmployeeRewardExcel> employeeRewardExcels = new ArrayList<>();
        //首先获取employee_reward表中的全部数据
        List<EmpReward> empRewards = list();
        empRewards.stream().filter(Objects::nonNull).forEach(empReward -> {
            //根据员工id从员工表中查出员工工号，员工姓名
            Long empId = empReward.getEmpId();
            Employee employee = Db.getById(empId, Employee.class);
            //根据file_id从Pdf文件表中查出file_name
            Long fileId = empReward.getFileId();
            PdfFile pdfFile = Db.getById(fileId, PdfFile.class);
            //根据岗位id从岗位表查询出岗位名
            Long positionId = empReward.getPositionId();
            Position position = Db.getById(positionId, Position.class);
            if (Check.noNull(employee, pdfFile, position)) {
                String employeeName = employee.getName();
                String num = employee.getNum();
                String fileName = pdfFile.getFileName();
                BigDecimal reward = empReward.getReward();
                String positionName = position.getPosition();
                EmployeeRewardExcel employeeRewardExcel = new EmployeeRewardExcel();
                employeeRewardExcel.setNum(num);
                employeeRewardExcel.setEmployeeName(employeeName);
                employeeRewardExcel.setFileName(fileName);
                employeeRewardExcel.setReward(reward);
                employeeRewardExcel.setPositionName(positionName);
                employeeRewardExcels.add(employeeRewardExcel);
            }
        });

        return employeeRewardExcels;
    }
}
