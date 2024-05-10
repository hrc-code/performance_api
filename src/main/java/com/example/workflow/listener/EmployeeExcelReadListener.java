package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.content.excel.EmployeeExcelUploadContent;
import com.example.workflow.entity.Dept;
import com.example.workflow.entity.EmpCoefficient;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.RegionCoefficient;
import com.example.workflow.entity.Role;
import com.example.workflow.pojo.EmployeeExcel;
import com.example.workflow.pojo.EmployeeExcelMsg;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EmployeeExcelReadListener implements ReadListener<EmployeeExcel> {
    /**
     * 每隔10条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 2000;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    /**
     * 缓存的数据
     */
    private List<EmployeeExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    public EmployeeExcelReadListener() {
    }

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data one row value. Is is same as {@link com.alibaba.excel.context.AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(EmployeeExcel data, AnalysisContext context) {
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

    /**
     * 向数据中保存数据
     */
    private void saveData() {
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            // 需要删除的错误信息  //将岗位不存在，地域不存在的排除
            Set<String> needDelNumSet = new HashSet<>();
            // 处理错误信息
            resolveValidateMsg(needDelNumSet);
            // 处理正确信息
            resolveOkMsg(needDelNumSet);
        }
    }

    // 处理正确信息
    private void resolveOkMsg(Set<String> needDelNumSet) {
        // 正常数据处理
        cachedDataList.stream().filter(Objects::nonNull)
                .filter(employeeExcel -> !needDelNumSet.contains(employeeExcel.getNum())).forEach(employeeExcel -> {
                    //向员工表添加记录
                    Employee employee = addEmployee(employeeExcel);
                    Long employeeId = employee.getId();
                    //向员工岗位表添加记录
                    addEmployeePosition(employeeExcel, employeeId);
                    //添加员工绩效表添加记录
                    addEmployeeCoefficient(employeeExcel, employeeId);
                });
    }

    //向员工岗位表添加记录
    private static void addEmployeePosition(EmployeeExcel employeeExcel, Long employeeId) {
        // 员工岗位表-员工表id-岗位表id-部门表id
        Long deptId = Db.lambdaQuery(Dept.class).eq(Dept::getDeptName, employeeExcel.getDeptName()).one().getId();
        // 向员工岗位表添加信息
        EmployeePosition employeePosition = new EmployeePosition();
        employeePosition.setEmpId(employeeId);

        Position posi = Db.lambdaQuery(Position.class)
                .eq(Position::getDeptId, deptId)
                .eq(Position::getTypeName, employeeExcel.getTypeName())
                .eq(Position::getPosition, employeeExcel.getPosition())
                .one();
        employeePosition.setPositionId(posi.getId());
        Short type = posi.getType();

        if (type == 5)
            employeePosition.setProcessKey("Process_1gzouwy");
        else if (type == 4)
            employeePosition.setProcessKey("Process_1whe0gq");
        else if (type == 3)
            employeePosition.setProcessKey("Process_01p7ac7");
        Db.save(employeePosition);
    }

    //向员工表添加信息
    private static Employee addEmployee(EmployeeExcel employeeExcel) {
        // 员工表-角色表id
        // 向员工表添加信息
        Employee employee = new Employee();
        employee.setNum(employeeExcel.getNum());
        employee.setName(employeeExcel.getName());
        employee.setPhoneNum1(employeeExcel.getPhoneNum1());
        employee.setPhoneNum2(employeeExcel.getPhoneNum2());
        employee.setEmail(employeeExcel.getEmail());
        employee.setIdNum(employeeExcel.getIdNum());
        employee.setBirthday(employeeExcel.getBirthday());
        employee.setAddress(employeeExcel.getAddress());
        // 去角色表查角色id
        Role role = Db.lambdaQuery(Role.class).eq(Role::getRoleName, employeeExcel.getRoleName()).one();
        employee.setRoleId(role.getId());

        Db.save(employee);
        return employee;
    }

    //添加员工绩效表添加记录
    private void addEmployeeCoefficient(EmployeeExcel employeeExcel, Long employeeId) {
        // 员工绩效表-员工表id-地区绩效表id
        // 向员工绩效表中添加数据
        EmpCoefficient empCoefficient = new EmpCoefficient();
        empCoefficient.setEmpId(employeeId);
        empCoefficient.setPositionCoefficient(employeeExcel.getPositionCoefficient());
        Long regionCoefficientId = Db.lambdaQuery(RegionCoefficient.class).eq(RegionCoefficient::getRegion, employeeExcel.getRegion())
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one().getId();
        empCoefficient.setRegionCoefficientId(regionCoefficientId);
        Db.save(empCoefficient);
    }

    // 参数校验
    private void resolveValidateMsg(Set<String> needDelNumSet) {
        // 错误员工信息
        ArrayList<EmployeeExcelMsg> errorEmployeeExcels = new ArrayList<>();
        cachedDataList.stream().filter(Objects::nonNull).forEach(employeeExcel -> {

            EmployeeExcelMsg employeeExcelMsg = new EmployeeExcelMsg();
            // 校验员工是否重复
            String num = validateNum(needDelNumSet, employeeExcel, employeeExcelMsg);
            //校验部门和岗位
            validateDeptAndPosition(needDelNumSet, employeeExcel, employeeExcelMsg, num);
            //校验地域
            validateRegion(needDelNumSet, employeeExcel, employeeExcelMsg, num);

            errorEmployeeExcels.add(employeeExcelMsg);
        });
        EmployeeExcelUploadContent.setErrorEmployeeList(errorEmployeeExcels);
    }

    //校验地域
    private void validateRegion(Set<String> needDelNumSet, EmployeeExcel employeeExcel, EmployeeExcelMsg employeeExcelMsg, String num) {
        // 查询地域是否存在
        String region = employeeExcel.getRegion();
        String name = employeeExcel.getName();
        if (region == null) {
            errorMsg(employeeExcelMsg, name + "填的地域为空\n", needDelNumSet, num);
        }
        RegionCoefficient one = Db.lambdaQuery(RegionCoefficient.class).eq(RegionCoefficient::getRegion, region)
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .one();
        if (one == null) {
            errorMsg(employeeExcelMsg, name + "的员工本月地域绩效不存\n", needDelNumSet, num);
        }
    }
    //校验部门和岗位
    private static void validateDeptAndPosition(Set<String> needDelNumSet, EmployeeExcel employeeExcel, EmployeeExcelMsg employeeExcelMsg, String num) {
        String deptName = employeeExcel.getDeptName();

        Position position = null;
        String name = employeeExcel.getName();
        if (StringUtils.isEmpty(deptName)) {
            errorMsg(employeeExcelMsg, name+"填的部门为空\n", needDelNumSet, num);
        } else {
            Dept dept = Db.lambdaQuery(Dept.class).eq(Dept::getDeptName, deptName).one();
            if (dept == null) {
                errorMsg(employeeExcelMsg, name+"所填的部门不存在\n", needDelNumSet, num);
            } else {
                Long deptId = dept.getId();
                // 去岗位表查询该岗位是否存在
                position = Db.lambdaQuery(Position.class)
                        .eq(Position::getDeptId, deptId)
                        .eq(Position::getTypeName, employeeExcel.getTypeName())
                        .eq(Position::getPosition, employeeExcel.getPosition()).one();
                if (position == null) {
                    errorMsg(employeeExcelMsg, name +"所填写的员工岗位不存在\n", needDelNumSet, num);
                }
            }
        }
    }

    // 校验员工是否重复
    private static String validateNum(Set<String> needDelNumSet, EmployeeExcel employeeExcel, EmployeeExcelMsg employeeExcelMsg) {
        Employee employee ;

        String num = employeeExcel.getNum();
        String name = employeeExcel.getName();
        if (num == null) {
            errorMsg(employeeExcelMsg, name+"的员工工号为空\n", needDelNumSet, num);
        } else {
            // 检查员工是否已经存在
            employee = Db.lambdaQuery(Employee.class).eq(Employee::getNum, num).one();
            if (employee != null) {
                errorMsg(employeeExcelMsg, name+"的员工工号已经存在\n", needDelNumSet, num);
            }
        }
        return num;
    }

    // 包裹错误信息
    private static void errorMsg(EmployeeExcelMsg employeeExcelMsg, String msg, Set<String> needDelNumSet, String num) {
        String originalMsg = employeeExcelMsg.getMsg();
        originalMsg = originalMsg == null ? "" : originalMsg;
        msg = msg  + originalMsg;
        employeeExcelMsg.setMsg(msg);
        needDelNumSet.add(num);
    }
}