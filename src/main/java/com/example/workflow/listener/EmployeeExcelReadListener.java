package com.example.workflow.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.entity.Dept;
import com.example.workflow.entity.EmpCoefficient;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.RegionCoefficient;
import com.example.workflow.entity.Role;
import com.example.workflow.pojo.EmployeeExcel;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EmployeeExcelReadListener implements ReadListener<EmployeeExcel> {
    /**
     * 每隔10条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 20;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    /**
     * 缓存的数据
     */
    private List<EmployeeExcel> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    public EmployeeExcelReadListener(){}

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data one row value. Is is same as {@link AnalysisContext#readRowHolder()}
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

    /** 向数据中保存数据*/
    private void saveData() {
        if (!CollectionUtils.isEmpty(cachedDataList)) {
            //需要删除的错误信息  //将岗位不存在，地域不存在的排除
            Set<String> needDelNumSet = new HashSet<>();
            cachedDataList.stream().filter(Objects::nonNull).forEach(employeeExcel -> {
                String num = employeeExcel.getNum();
                //检查是num是否存在
                Employee employee = Db.lambdaQuery(Employee.class).eq(Employee::getNum, num).one();
                String position = employeeExcel.getPosition();
                //去岗位表查询该岗位是否存在
                Position position1 = Db.lambdaQuery(Position.class).eq(Position::getPosition, position).one();
                if (employee != null) {
                    needDelNumSet.add(num);
                } else if (position1 == null) {
                    needDelNumSet.add(num);
                } else {
                    //查询地域是否存在
                    String region = employeeExcel.getRegion();
                    RegionCoefficient one = Db.lambdaQuery(RegionCoefficient.class).eq(RegionCoefficient::getRegion, region)
                            .apply(StringUtils.checkValNotNull(beginTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                            .apply(StringUtils.checkValNotNull(endTime),
                                    "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                            .one();
                    if (one == null) {
                        needDelNumSet.add(num);
                    }
                }
            });

            cachedDataList.stream().filter(Objects::nonNull).filter(employeeExcel -> !needDelNumSet.contains(employeeExcel.getNum())).forEach(employeeExcel -> {
                //员工表-角色表id
                //向员工表添加信息
                Employee employee = new Employee();
                employee.setNum(employeeExcel.getNum());
                employee.setName(employeeExcel.getName());
                employee.setPhoneNum1(employeeExcel.getPhoneNum1());
                employee.setPhoneNum2(employeeExcel.getPhoneNum2());
                employee.setEmail(employeeExcel.getEmail());
                employee.setIdNum(employeeExcel.getIdNum());
                employee.setBirthday(employeeExcel.getBirthday());
                employee.setAddress(employeeExcel.getAddress());
                //去角色表查角色id
                Role role = Db.lambdaQuery(Role.class).eq(Role::getRoleName, employeeExcel.getRoleName()).one();
                employee.setRoleId(role.getId());
                Db.save(employee);

                Long employeeId = employee.getId();

                // 员工岗位表-员工表id-岗位表id-部门表id
                Long deptId = Db.lambdaQuery(Dept.class).eq(Dept::getDeptName, employeeExcel.getDeptName()).one().getId();
                //向员工岗位表添加信息
                EmployeePosition employeePosition = new EmployeePosition();
                employeePosition.setEmpId(employeeId);

                Long positionId = Db.lambdaQuery(Position.class).eq(Position::getDeptId, deptId).eq(Position::getTypeName, employeeExcel.getTypeName()).eq(Position::getPosition, employeeExcel.getPosition()).one().getId();
                employeePosition.setPositionId(positionId);
                Db.save(employeePosition);

                // 员工绩效表-员工表id-地区绩效表id
                //向员工绩效表中添加数据
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
            });
        }
        }
    }