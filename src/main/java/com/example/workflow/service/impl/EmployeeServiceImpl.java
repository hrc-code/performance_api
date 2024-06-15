package com.example.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.common.R;
import com.example.workflow.dto.EmployeeFormDto;
import com.example.workflow.entity.Button;
import com.example.workflow.entity.Dept;
import com.example.workflow.entity.DeptHierarchy;
import com.example.workflow.entity.EmpCoefficient;
import com.example.workflow.entity.EmpKpi;
import com.example.workflow.entity.EmpOkr;
import com.example.workflow.entity.EmpPiece;
import com.example.workflow.entity.EmpReward;
import com.example.workflow.entity.EmpScore;
import com.example.workflow.entity.Employee;
import com.example.workflow.entity.EmployeePosition;
import com.example.workflow.entity.Position;
import com.example.workflow.entity.PositionAssessor;
import com.example.workflow.entity.RegionCoefficient;
import com.example.workflow.entity.Role;
import com.example.workflow.entity.RoleBtn;
import com.example.workflow.entity.ScoreAssessors;
import com.example.workflow.mapper.EmployeeMapper;
import com.example.workflow.mapper.EmployeePositionMapper;
import com.example.workflow.pojo.DeptIdAndNape;
import com.example.workflow.pojo.EmpIdAndStateId;
import com.example.workflow.pojo.EmployeeExcel;
import com.example.workflow.pojo.PostIdAndName;
import com.example.workflow.pojo.PostIdPercent;
import com.example.workflow.service.ButtonService;
import com.example.workflow.service.DeptService;
import com.example.workflow.service.EmpCoefficientService;
import com.example.workflow.service.EmpKpiService;
import com.example.workflow.service.EmpOkrService;
import com.example.workflow.service.EmpPieceService;
import com.example.workflow.service.EmpRewardService;
import com.example.workflow.service.EmpScoreService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.EmployeeService;
import com.example.workflow.service.PositionAssessorService;
import com.example.workflow.service.PositionService;
import com.example.workflow.service.RegionCoefficientService;
import com.example.workflow.service.RoleBtnService;
import com.example.workflow.service.RoleService;
import com.example.workflow.service.ScoreAssessorsService;
import com.example.workflow.utils.DateTimeUtils;
import com.example.workflow.utils.Find;
import com.example.workflow.vo.EmployeeVo;
import com.example.workflow.vo.RouterAndButtonVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author hrc
 * @description 针对表【employee(员工)】的数据库操作Service实现
 * @createDate 2024-03-27 23:29:44
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final EmployeePositionService employeePositionService;

    private final EmployeePositionMapper employeePositionMapper;

    private final DeptService deptService;

    private final PositionService positionService;

    private final RegionCoefficientService regionCoefficientService;

    private final EmpCoefficientService empCoefficientService;

    private final RoleBtnService roleBtnService;

    private final ButtonService buttonService;

    private final RoleService roleService;

    @Autowired
    private PositionAssessorService PositionAssessorService;
    @Autowired
    private ScoreAssessorsService ScoreAssessorsService;
    @Autowired
    private EmpKpiService EmpKpiService;
    @Autowired
    private EmpPieceService EmpPieceService;
    @Autowired
    private EmpRewardService EmpRewardService;
    @Autowired
    private EmpScoreService EmpScoreService;
    @Autowired
    private EmpOkrService EmpOkrService;

    //改变员工账号状态
    @Override
    public void setStateToEmployee(List<EmpIdAndStateId> empIdAndStateIdList) {
        //stateId = 0 停用
        Set<Long> state0EmpSet = empIdAndStateIdList.stream().filter(empIdAndStateId -> empIdAndStateId.getStateId().equals(Short.valueOf("0"))).map(EmpIdAndStateId::getEmpId).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(state0EmpSet)){
            lambdaUpdate().set(Employee::getState, 0).in(Employee::getId,state0EmpSet).update();
        }
        //stateId = 1 恢复
        Set<Long> state1EmpSet = empIdAndStateIdList.stream().filter(empIdAndStateId -> empIdAndStateId.getStateId().equals(Short.valueOf("1"))).map(EmpIdAndStateId::getEmpId).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(state1EmpSet)){
            lambdaUpdate().set(Employee::getState, 1).in(Employee::getId,state1EmpSet).update();
        }
    }

    /**
     * 根据部门id获取 该部门下以及后代部门的的全部员工
     */
    @Override
    public Collection<EmployeeExcel> getEmployeeExcels(Long deptId, String name, String num) {
        List<Employee> employees;
        ArrayList<EmployeeExcel> employeeExcels = new ArrayList<>();

        // 去部门表判断部门id是否存在，不存在就返回全部数据
        if (Objects.nonNull(deptId)) {
            Dept dept = deptService.getById(deptId);
            if (Objects.isNull(dept)) {
                employees = list();
            } else {
                // 根据部门id获取 该部门下以及后代部门的的全部员工
                employees = getEmployeeListByDeptId(deptId);
            }
        } else {
            employees = lambdaQuery().like(Objects.nonNull(name), Employee::getName, name).like(Objects.nonNull(num), Employee::getNum, num).list();
        }

        return wrapperEmployeeToEmployeeExcel(employees, employeeExcels);
    }

    //  将员工基本信息和其他信息包装成excel
    private ArrayList<EmployeeExcel> wrapperEmployeeToEmployeeExcel(List<Employee> employees, ArrayList<EmployeeExcel> employeeExcels) {
        // 查询员工的其他信息
        if (!CollectionUtils.isEmpty(employees)) {
            for (Employee employee : employees) {
                EmployeeExcel employeeExcel = new EmployeeExcel();
                BeanUtils.copyProperties(employee, employeeExcel);
                Long employeeId = employee.getId();
                // 查询职务名
                Long roleId = employee.getRoleId();
                Role role = roleService.getById(roleId);
                if (Objects.nonNull(role)) {
                    employeeExcel.setRoleName(role.getRoleName());
                }
                // 向返回对象设置岗位和部门信息
                setPositionAndDeptToEmployeeExcel(employeeId, employeeExcel);
                // 向返回对象设置地域和岗位绩效
                setRegionAndPositionCoefficientToEmployeeExcel(employeeId, employeeExcel);
                employeeExcels.add(employeeExcel);
            }
        }
        return employeeExcels;
    }

    // 向返回对象设置地域和岗位绩效
    private void setRegionAndPositionCoefficientToEmployeeExcel(Long employeeId, EmployeeExcel employeeExcel) {
        LocalDateTime[] times = DateTimeUtils.getTheStartAndEndTimeOfMonth();
        EmpCoefficient empCoefficient = empCoefficientService.lambdaQuery().eq(EmpCoefficient::getEmpId, employeeId).between(EmpCoefficient::getCreateTime, times[0], times[1]).one();
        if (Objects.nonNull(empCoefficient)) {
            BigDecimal positionCoefficient = empCoefficient.getPositionCoefficient();
            employeeExcel.setPositionCoefficient(positionCoefficient);
            // 地域名
            Long regionCoefficientId = empCoefficient.getRegionCoefficientId();
            RegionCoefficient regionCoefficient = regionCoefficientService.getById(regionCoefficientId);
            if (Objects.nonNull(regionCoefficient)) {
                String region = regionCoefficient.getRegion();
                employeeExcel.setRegion(region);
            }
        }
    }

    // 向返回对象设置岗位和部门信息
    private void setPositionAndDeptToEmployeeExcel(Long employeeId, EmployeeExcel employeeExcel) {
        List<EmployeePosition> employeePositionList = employeePositionService.lambdaQuery().eq(EmployeePosition::getEmpId, employeeId).list();
        if (!CollectionUtils.isEmpty(employeePositionList)) {
            EmployeePosition employeePosition = employeePositionList.get(0);
            Long positionId = employeePosition.getPositionId();
            Position position = positionService.getById(positionId);
            if (Objects.nonNull(position)) {
                String positionName = position.getPosition();
                String typeName = position.getTypeName();
                employeeExcel.setPosition(positionName);
                employeeExcel.setTypeName(typeName);
                // 查询部门名
                Long deptId = position.getDeptId();
                Dept dept = deptService.getById(deptId);
                if (Objects.nonNull(dept)) {
                    String deptName = dept.getDeptName();
                    employeeExcel.setDeptName(deptName);
                }
            }
        }
    }

    /**
     * 注意：当这个部门不存在，部门没有岗位时会查询全部数据
     * 根据部门id获取 该部门下以及后代部门的的全部员工
     */
    private List<Employee> getEmployeeListByDeptId(Long deptId) {
        List<Employee> employees;
        // 查询这个部门的全部子部门id
        Set<Long> childeDeptIdSet = deptService.getChildeDeptIdSet(deptId);
        childeDeptIdSet.add(deptId);
        List<Position> positionList = positionService.lambdaQuery().in(Position::getDeptId, childeDeptIdSet).list();
        Set<Long> positionIdSet = positionList.stream().map(Position::getId).collect(Collectors.toSet());
        List<EmployeePosition> employeePositions = employeePositionService.lambdaQuery().in(!CollectionUtils.isEmpty(positionIdSet), EmployeePosition::getPositionId, positionIdSet).list();
        Set<Long> empIdSet = employeePositions.stream().map(EmployeePosition::getEmpId).collect(Collectors.toSet());
        employees = lambdaQuery().in(!CollectionUtils.isEmpty(empIdSet), Employee::getId, empIdSet).list();
        return employees;
    }

    /**
     * 通过名字, 工号获取员工信息 可以获得不完整信息
     */

    @Override
    public List<EmployeeVo> getList(String name, String num,Long roleId,Long id) {
        List<EmployeeVo> employeeVos = new ArrayList<>();

        // 获取员工基本信息
        List<Employee> employees = lambdaQuery()
                .like(Objects.nonNull(name), Employee::getName, name)
                .like(Objects.nonNull(num), Employee::getNum, num)
                .eq(Objects.nonNull(roleId), Employee::getRoleId, roleId)
                .eq(Objects.nonNull(id), Employee::getId, id)
                .list();
        // 为员工添加额外信息
        wrapperToEmployeeVo(employees, employeeVos);
        return employeeVos;
    }

    // 为员工添加额外信息
    private void wrapperToEmployeeVo(List<Employee> employees, List<EmployeeVo> employeeVos) {
        if (!CollectionUtils.isEmpty(employees)) {
            for (Employee employee : employees) {
                EmployeeVo employeeVo = new EmployeeVo();
                BeanUtils.copyProperties(employee, employeeVo);
                // 获得职务名
                Long roleId = employee.getRoleId();
                Role role = roleService.getById(roleId);
                // 允许职务名为空
                String roleName;
                if (Objects.nonNull(role)) {
                    roleName = role.getRoleName();
                    employeeVo.setRoleName(roleName);
                }
                // 为返回结果设置地域
                Long employeeId = setRegionFoEmployeeVo(employee, employeeVo);
                // 为返回值设置岗位名集合和部门名集合
                setPositionNamesAndDeptNamesForEmployeeVo(employeeId, employeeVo);
                employeeVos.add(employeeVo);
            }
        }
    }

    // 为返回值设置岗位名集合和部门名集合
    private void setPositionNamesAndDeptNamesForEmployeeVo(Long employeeId, EmployeeVo employeeVo) {
        // 获取岗位名
        LambdaQueryWrapper<EmployeePosition> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(EmployeePosition::getEmpId, employeeId);
        List<EmployeePosition> employeePositionList = employeePositionService.list(wrapper2);
        // 允许岗位名为空
        ArrayList<String> positionNames;
        Collection<String> deptNames;
        if (CollectionUtil.isNotEmpty(employeePositionList)) {
            // position_id 设置为 not null,所以集合不会为空
            Set<Long> positionIdList = employeePositionList.stream().filter(Objects::nonNull).map(EmployeePosition::getPositionId).collect(Collectors.toSet());
            List<Position> positions = positionService.listByIds(positionIdList);
            if (CollectionUtil.isNotEmpty(positions)) {
                positionNames = positions.stream().map(Position::getPosition).distinct().collect(Collectors.toCollection(ArrayList::new));
                employeeVo.setPostName(positionNames);

                // 查询部门名
                List<Long> detpIdList = positions.stream().map(Position::getDeptId).distinct().collect(Collectors.toList());
                deptNames = deptService.getDeptNameMap(detpIdList).values();
                employeeVo.setDeptNames(new ArrayList<>(deptNames));
            }
        }
    }

    // 为返回结果设置地域
    private Long setRegionFoEmployeeVo(Employee employee, EmployeeVo employeeVo) {
        // 获取地域
        Long employeeId = employee.getId();
        LocalDateTime[] times = DateTimeUtils.getTheStartAndEndTimeOfMonth();

        LambdaQueryWrapper<EmpCoefficient> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(EmpCoefficient::getCreateTime, times[0], times[1]).eq(EmpCoefficient::getEmpId, employeeId);
        EmpCoefficient empCoefficient = empCoefficientService.getOne(wrapper);
        // 允许地域为空
        String region;
        if (Objects.nonNull(empCoefficient)) {
            Long regionCoefficientId = empCoefficient.getRegionCoefficientId();
            RegionCoefficient regionCoefficient = regionCoefficientService.getById(regionCoefficientId);
            region = regionCoefficient.getRegion();
            employeeVo.setRegionName(region);
        }
        return employeeId;
    }

    /*
     * 根据ceoId查询其下的全部员工
     * ceoId 即ceo的员工  不包括ceo自己  该部门级别以下（包括该部门）
     * 参数：emp_id（该员工一定一定是某部门负责人，也就是ceo）,
     * 返回值：该员工所属部门（如果该部门是三级单元，则所属部门包括三、四级单元；
     * 如果该部门是二级单元，则所属部门包括二、三、四级单元；如果该部门是四级单元，则所属部门包括四级单元）下的“所有员工d*/
    @Override
    public R allByCeoId(Long ceoId) {
        ArrayList<EmployeeVo> employeeVos = new ArrayList<>();
        HashSet<Long> idSet = new HashSet<>();
        // 首先根据ceoId去员工岗位表查询全部岗位id
        List<EmployeePosition> employeePositions = employeePositionService.lambdaQuery().eq(EmployeePosition::getEmpId, ceoId).list();
        if (!CollectionUtils.isEmpty(employeePositions)) {
            employeePositions.stream().filter(Objects::nonNull).forEach(employeePosition -> {
                Long positionId = employeePosition.getPositionId();
                // 在根据岗位id去岗位表查询type < 4 的部门id
                Position position = Db.getById(positionId, Position.class);
                if (position != null) {
                    Short type = position.getType();
                    // 不包括此ceo不当其他部门的ceo
                    if (type < 4) {
                        Long deptId = position.getDeptId();
                        //  根据部门id查询该部门下的全部员工 同部门
                        idSet.addAll(getEmployeeIds(deptId));
                        // 部门级别以下  根据部门id 去 部门继承表中查询全部子部门id
                        HashSet<Long> ids = new HashSet<>();
                        getDeptIds(ids, deptId);
                        if (!CollectionUtils.isEmpty(ids)) {
                            ids.stream().filter(Objects::nonNull).forEach(id -> {
                                Set<Long> employeeIds = getEmployeeIds(id);
                                if (!CollectionUtils.isEmpty(employeeIds)) {
                                    idSet.addAll(employeeIds);
                                }
                            });
                        }
                    }
                }
            });
        }
        // 最后根据员工id去 员工表 查询 员工name
        // 不包括ceo自己
        idSet.remove(ceoId);
        idSet.stream().filter(Objects::nonNull).forEach(id -> {
            Employee employee = getById(id);
            if (employee != null) {
                String name = employee.getName();
                EmployeeVo employeeVo = new EmployeeVo();
                employeeVo.setId(id);
                employeeVo.setName(name);
                employeeVos.add(employeeVo);
            }
        });
        return R.success(employeeVos);
    }

    /**
     * 获取该部门级别 以下的全部部门id  用递归法
     */
    private void getDeptIds(Set<Long> ids, Long deptId) {
        // 当根据部门id去部门表差不到数据时结束递归
        List<DeptHierarchy> deptHierarchies = Db.lambdaQuery(DeptHierarchy.class).eq(DeptHierarchy::getParentId, deptId).list();
        if (CollectionUtils.isEmpty(deptHierarchies)) {
            return;
        }
        deptHierarchies.stream().filter(Objects::nonNull).forEach(deptHierarchy -> {
            Long childId = deptHierarchy.getChildId();
            ids.add(childId);
            getDeptIds(ids, childId);
        });
    }

    // 根据部门id查询该部门下的全部员工
    private Set<Long> getEmployeeIds(Long deptId) {
        HashSet<Long> idSet = new HashSet<>();
        // 根据部门id去岗位表查询全部岗位
        List<Position> positions = positionService.lambdaQuery().eq(Position::getDeptId, deptId).list();
        // 根据岗位id去员工岗位表查询该岗位全部员工id，并且加入set中
        positions.stream().filter(Objects::nonNull).forEach(position -> {
            Long positionId = position.getId();
            List<EmployeePosition> employeePositions = employeePositionService.lambdaQuery().eq(EmployeePosition::getPositionId, positionId).list();
            if (employeePositions != null) {
                employeePositions.stream().filter(Objects::nonNull).forEach(employeePosition -> {
                    Long empId = employeePosition.getEmpId();
                    idSet.add(empId);
                });
            }
        });
        return idSet;
    }

    /**
     * 操作 员工表  员工岗位表  员工绩效表
     * 获取要修改员工的个人信息
     * id  员工id
     */
    @Override
    public R getInfoById(Long id) {
        EmployeeVo employeeVo = new EmployeeVo();
        Employee employee = getById(id);
        BeanUtils.copyProperties(employee, employeeVo);

        ArrayList<PostIdPercent> postIdPercents = new ArrayList<>();
        ArrayList<PostIdAndName> postIdAndNames = new ArrayList<>();
        // 根据员工id 去 员工岗位表 查询全部的岗位id percent
        List<EmployeePosition> employeePositions = employeePositionService.lambdaQuery().eq(EmployeePosition::getEmpId, id).list();
        // 查询整个岗位列表
        List<Position> positions = positionService.list();
        HashMap<Long, String> positionMap = new HashMap<>();

        // 哈希表降低查询岗位名称的时间复杂度
        for (Position position : positions) positionMap.put(position.getId(), position.getPosition());

        if (employeePositions != null) {
            employeePositions.stream().filter(Objects::nonNull).forEach(employeePosition -> {
                Long positionId = employeePosition.getPositionId();
                BigDecimal posiPercent = employeePosition.getPosiPercent();
                PostIdPercent postIdPercent = new PostIdPercent();
                postIdPercent.setPercent(posiPercent);
                postIdPercent.setPostId(positionId);
                postIdPercents.add(postIdPercent);
                // 将该员工所属的岗位id和岗位名称添加到postIdAndNames集合中
                PostIdAndName postIdAndName = new PostIdAndName();
                postIdAndName.setId(positionId);
                postIdAndName.setPosition(positionMap.get(positionId));
                postIdAndNames.add(postIdAndName);
            });
            employeeVo.setPosts(postIdAndNames);
            employeeVo.setPostIdPercent(postIdPercents);


            LocalDateTime[] time = DateTimeUtils.getTheStartAndEndTimeOfMonth();
            // 根据员工id 去 员工绩效表 查询岗位绩效， 地区id
            EmpCoefficient empCoefficient = Db.lambdaQuery(EmpCoefficient.class).eq(EmpCoefficient::getEmpId, id).between(EmpCoefficient::getUpdateTime, time[0], time[1]).one();
            if (empCoefficient != null) {
                BigDecimal positionCoefficient = empCoefficient.getPositionCoefficient();
                Long regionCoefficientId = empCoefficient.getRegionCoefficientId();
                // 设置岗位id和岗位名称集合
                employeeVo.setRegionId(regionCoefficientId);
                employeeVo.setGrade(positionCoefficient);
            }
        }
        return R.success(employeeVo);
    }

    /**
     * 操作  员工表  员工绩效表  部门表  岗位表  员工岗位表
     * 根据员工id 获取信息
     */
    @Override
    public R infoById(Long id) {
        EmployeeVo employeeVo = new EmployeeVo();
        Employee employee = getById(id);
        ArrayList<String> deptNames = new ArrayList<>();
        ArrayList<String> postNames = new ArrayList<>();
        // 根据员工id 去 员工岗位表 查询这名员工的全部岗位
        List<EmployeePosition> employeePositions = employeePositionService.lambdaQuery().eq(EmployeePosition::getEmpId, id).list();
        if (employeePositions != null) {
            employeePositions.stream().filter(Objects::nonNull).forEach(employeePosition -> {
                // 根据岗位id 去 岗位表 查寻 部门id 岗位名
                Long positionId = employeePosition.getPositionId();
                Position position = positionService.getById(positionId);
                if (position != null) {
                    Long deptId = position.getDeptId();
                    String positionName = position.getPosition();
                    // 根据部门id 调用 getDeptName() 获取 树形部门名
                    String deptName = deptService.getDeptName(deptId);
                    // 收集部门名， 岗位名
                    deptNames.add(deptName);
                    postNames.add(positionName);
                }
            });
            // 根据员工id 去员工绩效表查询岗位绩效
            LocalDateTime[] time = DateTimeUtils.getTheStartAndEndTimeOfMonth();
            EmpCoefficient empCoefficient = Db.lambdaQuery(EmpCoefficient.class).eq(EmpCoefficient::getEmpId, id).between(EmpCoefficient::getUpdateTime, time[0], time[1]).one();
            if (empCoefficient != null) {
                BigDecimal grade = empCoefficient.getPositionCoefficient();
                // 只有查到全部数据才设置返回值
                BeanUtils.copyProperties(employee, employeeVo);
                employeeVo.setGrade(grade);
                employeeVo.setPostName(postNames);
                employeeVo.setDeptNames(deptNames);
                return R.success(employeeVo);
            } else {
                return R.error("该员工没有本月绩效");
            }
        }
        return R.error("该名员工数据不足");
    }

    /**
     * 查询员工全部信息  操作  员工表  部门表  岗位表  地域绩效表
     */

    @Override
    @Deprecated
    public R page(EmployeeFormDto employeeFormDto) {

        String num = employeeFormDto.getNum();
        String name = employeeFormDto.getName();
        Long deptId = employeeFormDto.getDeptId();
        Long roleId = employeeFormDto.getRoleId();

        // 获取全部满足条件的员工
        List<Employee> employees = Db.lambdaQuery(Employee.class).like(num != null, Employee::getNum, num).like(name != null, Employee::getName, name).eq(roleId != null, Employee::getRoleId, roleId).eq(Employee::getState, 1).list();

        if (CollectionUtils.isEmpty(employees)) {
            return R.error("没有员工");
        }
        // 根据员工id分组后的全部员工
        Map<Long, List<Employee>> employeeMap = employees.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(Employee::getId));
        if (CollectionUtils.isEmpty(employeeMap)) {
            return R.error("没有员工");
        }
        List<DeptHierarchy> deptHierarchys = Db.lambdaQuery(DeptHierarchy.class).list();

        Map<Long, Long> deptHierarchyMap = new HashMap<>();

        for (DeptHierarchy deptHierarchy : deptHierarchys) {
            deptHierarchyMap.put(deptHierarchy.getChildId(), deptHierarchy.getParentId());
        }

        // 返回结果集
        ArrayList<EmployeeVo> employeeVos = new ArrayList<>();

        // 员工id集合
        List<Long> employeeIds = employees.stream().filter(Objects::nonNull).map(Employee::getId).collect(Collectors.toList());
        LocalDateTime[] time = DateTimeUtils.getTheStartAndEndTimeOfMonth();
        // 全部员工绩效记录 只能获取本月的员工绩效
        List<EmpCoefficient> allEmpCoefficients = empCoefficientService.lambdaQuery().in(EmpCoefficient::getEmpId, employeeIds).between(EmpCoefficient::getUpdateTime, time[0], time[1]).list();
        if (CollectionUtils.isEmpty(allEmpCoefficients)) {
            return R.error("员工本月没有绩效");

        }
        // 全部地域id
        Set<Long> regionCoefficientIdSet = allEmpCoefficients.stream().filter(Objects::nonNull).map(EmpCoefficient::getRegionCoefficientId).collect(Collectors.toSet());
        List<RegionCoefficient> allRegionCoefficient = regionCoefficientService.lambdaQuery().in(RegionCoefficient::getId, regionCoefficientIdSet).list();
        if (CollectionUtils.isEmpty(allRegionCoefficient)) {
            return R.error("没有地域绩效数据");
        }
        // 根据地域id分组后的地域绩效记录
        Map<Long, List<RegionCoefficient>> regionCoefficientMap = allRegionCoefficient.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(RegionCoefficient::getId));
        // 根据员工id分组后的员工绩效  只有一条
        Map<Long, List<EmpCoefficient>> empCoefficientMap = allEmpCoefficients.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(EmpCoefficient::getEmpId));
        // 全部员工岗位记录
        List<EmployeePosition> allEmployeePositions = employeePositionService.lambdaQuery().in(EmployeePosition::getEmpId, employeeIds).list();
        if (CollectionUtil.isEmpty(allEmployeePositions)) {
            return R.error("员工没有岗位");
        }
        // 全部的岗位id
        Set<Long> positionIdSet = allEmployeePositions.stream().filter(Objects::nonNull).map(EmployeePosition::getPositionId).collect(Collectors.toSet());
        // 全部岗位
        List<Position> positions = positionService.lambdaQuery().in(Position::getId, positionIdSet).list();
        // 全部部门id
        Set<Long> deptIdSet = positions.stream().filter(Objects::nonNull).map(Position::getDeptId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(deptIdSet)) {
            return R.error("岗位没有部门");
        }
        // 全部部门名
        List<Long> deptIdList = deptIdSet.stream().filter(Objects::nonNull).collect(Collectors.toList());
        Map<Long, String> deptNameMap = deptService.getDeptNameMap(deptIdList);
        HashMap<Long, String> deptIdNameMap = new HashMap<>();
        deptIdSet.stream().filter(Objects::nonNull).forEach(deptIdOne -> {
            // 再根据部门id调用deptService的getDeptName()获得部门name
            String deptName = deptNameMap.get(deptIdOne);
            deptIdNameMap.put(deptIdOne, deptName);
        });

        // 查询所有权限（角色）列表
        List<Role> roles = Db.lambdaQuery(Role.class).list();
        // 将角色转换为map，提高检索时的效率
        Map<Long, String> roleMap = new HashMap<>();
        for (Role role : roles) roleMap.put(role.getId(), role.getRoleName());

        // 根据岗位id分组之后的岗位
        Map<Long, List<Position>> positionMap = positions.stream().collect(Collectors.groupingBy(Position::getId));
        // 根据员工id分组后的全部岗位
        Map<Long, List<EmployeePosition>> employeeIdAndEmployeePositionMap = allEmployeePositions.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(EmployeePosition::getEmpId));
        employeeIdAndEmployeePositionMap.forEach((employeeId, employeePositions) -> {
            ArrayList<Long> positionIds = new ArrayList<>();
            ArrayList<String> postNames = new ArrayList<>();
            ArrayList<Long> deptIds = new ArrayList<>();
            ArrayList<String> deptNames = new ArrayList<>();

            // 是否匹配deptId
            AtomicReference<Boolean> hasDeptIdFlag = new AtomicReference<>(false);

            if (employeePositions != null) {
                employeePositions.stream().filter(Objects::nonNull).forEach(employeePosition -> {
                    // 在根据岗位id去岗位表查询岗位名，部门id
                    Long positionId = employeePosition.getPositionId();
                    List<Position> positionList = positionMap.get(positionId);
                    if (!CollectionUtils.isEmpty(positionList)) {
                        // 一个岗位id对应一个岗位，所以只会循环一次
                        positionList.stream().filter(Objects::nonNull).forEach(position -> {
                            String postName = position.getPosition();
                            Long deptId1 = position.getDeptId();
                            // 再根据部门id调用deptService的getDeptName()获得部门name
                            String deptName = deptIdNameMap.get(deptId1);

                            if (Find.findPathDeptId(deptHierarchyMap, deptId, deptId1)) hasDeptIdFlag.set(true);
                            positionIds.add(positionId);
                            postNames.add(postName);
                            deptIds.add(deptId1);
                            deptNames.add(deptName);
                        });
                    }
                });
            }

            // 1. dept_Id 为空    2.查出来的员工部门id != dept_id   3. 相等  再次根据部门id筛选符合条件的员工
            // 不相等就将这名员工过滤，不符合条件 就是不做处理
            if (deptId == null || hasDeptIdFlag.get()) {
                // 只会循环一次  一个员工只有一条员工绩效记录
                List<EmpCoefficient> empCoefficients = empCoefficientMap.get(employeeId);
                if (!CollectionUtils.isEmpty(empCoefficients)) {
                    empCoefficients.stream().filter(Objects::nonNull).forEach(empCoefficient -> {
                        // 根据员工id去员工绩效表查询地域绩效id
                        Long regionCoefficientId = empCoefficient.getRegionCoefficientId();
                        // 根据地域绩效id去地域绩效表查询地域名，地域id
                        List<RegionCoefficient> regionCoefficients = regionCoefficientMap.get(regionCoefficientId);
                        if (!CollectionUtils.isEmpty(regionCoefficients)) {
                            // 只循环一次--地域绩效id对应一条数据
                            regionCoefficients.stream().filter(Objects::nonNull).forEach(regionCoefficient -> {
                                List<Employee> employeeList = employeeMap.get(employeeId);
                                Long id = regionCoefficient.getId();
                                String region = regionCoefficient.getRegion();
                                EmployeeVo employeeVo = new EmployeeVo();
                                // 循环一次， 一个员工id对应一名员工
                                employeeList.stream().filter(Objects::nonNull).forEach(employee -> {
                                    BeanUtils.copyProperties(employee, employeeVo);
                                    // 将权限名称设置进去
                                    employeeVo.setRoleName(roleMap.get(employee.getRoleId()));
                                });
                                employeeVo.setDeptIds(deptIds);
                                employeeVo.setDeptNames(deptNames);
                                employeeVo.setPostId(positionIds);
                                employeeVo.setPostName(postNames);
                                employeeVo.setRegionId(id);
                                employeeVo.setRegionName(region);
                                employeeVos.add(employeeVo);
                            });
                        }
                    });
                }
            }
        });
        return R.success(employeeVos);
    }


    /**
     * 添加员工  操作  员工表  员工岗位表  员工绩效表
     */
    @Transactional
    @Override
    public R addEmployee(EmployeeFormDto employeeFormDto) {
        // 员工号不嫩重复
        String num = employeeFormDto.getNum();

        Employee one = Db.lambdaQuery(Employee.class).eq(Employee::getNum, num).one();
        if (one != null) {
            return R.error("员工号重复");
        }
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeFormDto, employee);
        save(employee);
        log.info(employee.toString());

        List<PostIdPercent> postIdPercentList = employeeFormDto.getPostIdPercent();
        // 岗位所对应的绩效
        Map<Long, BigDecimal> map = postIdPercentList.stream().collect(Collectors.toMap(PostIdPercent::getPostId, PostIdPercent::getPercent));
        map.forEach((postId, percent) -> {
            // 向员工岗位表插入数据
            EmployeePosition employeePosition = new EmployeePosition();
            employeePosition.setEmpId(employee.getId());
            employeePosition.setState(1);
            employeePosition.setPositionId(postId);
            employeePosition.setPosiPercent(map.get(postId));

            Short type = Db.lambdaQuery(Position.class).eq(Position::getId, postId).one().getType();
            if (type == 5) employeePosition.setProcessKey("Process_1gzouwy");
            else if (type == 4) employeePosition.setProcessKey("Process_1whe0gq");
            else if (type == 3) employeePosition.setProcessKey("Process_01p7ac7");
            employeePositionService.save(employeePosition);
        });

        // 向员工绩效表中添加数据
        EmpCoefficient empCoefficient = new EmpCoefficient();
        empCoefficient.setEmpId(employee.getId());
        empCoefficient.setRegionCoefficientId(employeeFormDto.getRegionId());
        empCoefficient.setPositionCoefficient(employeeFormDto.getGrade());

        Db.save(empCoefficient);

        return R.success();
    }

    /**
     * 查询员工  擦作 员工表  员工岗位表 部门表  岗位表 地域绩效表
     * id 员工id
     */
    @Override
    @Deprecated
    public R getEmployeeById(List<Long> ids) {
        List<EmployeeVo> result = new ArrayList<>();

        for (Long id : ids) {
            Employee employee = getById(id);
            if (employee == null) {
                return R.error("员工号错误");
            }
            // 员工与岗位为多对多关系
            List<EmployeePosition> employeePositions = employeePositionService.lambdaQuery().eq(EmployeePosition::getEmpId, employee.getId()).list();
            EmployeeVo employeeVo = new EmployeeVo();
            List<Long> postIds = new ArrayList<>();
            ArrayList<String> postNames = new ArrayList<>();
            // 员工基本属性
            BeanUtils.copyProperties(employee, employeeVo);
            // 全部部门ids 全部部门名称
            ArrayList<DeptIdAndNape> deptIdAndNapes = new ArrayList<>();
            // 遍历员工的全部岗位
            List<PostIdPercent> postIdPercentList = employeePositions.stream().map(employeePosition -> {
                PostIdPercent postIdPercent = new PostIdPercent();
                Long positionId = employeePosition.getPositionId();
                // 查询岗位表
                Position position = positionService.getById(positionId);
                postNames.add(position.getPosition());
                postIds.add(positionId);
                postIdPercent.setPostId(positionId);
                postIdPercent.setPercent(employeePosition.getPosiPercent());

                ArrayList<Long> deptIds = new ArrayList<>();
                StringJoiner deptNames = new StringJoiner(",");
                // 获取全部部门ids 全部部门名称
                deptService.getAllSuperiorDept(positionId).getData().forEach(deptId -> {
                    deptIds.add(deptId);
                    if (deptId != null) {
                        String deptName = deptService.getById(deptId).getDeptName();
                        deptNames.add(deptName);
                    }
                });
                DeptIdAndNape deptIdAndNape = new DeptIdAndNape();
                deptIdAndNape.setDeptIds(deptIds);
                deptIdAndNape.setDeptName(deptNames.toString());
                deptIdAndNapes.add(deptIdAndNape);

                // 根据岗位id去岗位表查询部门id
                Long deptId = positionService.getById(positionId).getDeptId();
                // 最后根据部门id去部门表查询部门名称
                employeeVo.setDeptName(deptService.getDeptName(deptId));
                return postIdPercent;
            }).collect(Collectors.toList());


            // 设置部门ids 部门名
            employeeVo.setDeptIdAndNapeList(deptIdAndNapes);
            // 岗位绩效
            employeeVo.setPostIdPercent(postIdPercentList);
            // 岗位id
            employeeVo.setPostId(postIds);
            // 岗位名称
            employeeVo.setPostName(postNames);

            // 地域名称
            Long regionCoefficientId = empCoefficientService.lambdaQuery().eq(EmpCoefficient::getEmpId, id).one().getRegionCoefficientId();
            RegionCoefficient regionCoefficient = regionCoefficientService.getById(regionCoefficientId);
            employeeVo.setRegionName(regionCoefficient.getRegion());

            result.add(employeeVo);
        }


        return R.success(result);
    }


    /**
     * 更新员工  操作  员工表  员工岗位表    角色表  地域系数表 员工系数表
     */
    @Transactional
    @Override
    public R updateEmployee(EmployeeFormDto employeeFormDto) {
        Long id = employeeFormDto.getId();
        // 更新员工基本信息
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeFormDto, employee);

        updateById(employee);

        // 先将所有empId相等的数据都删除
        QueryWrapper<EmployeePosition> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("emp_id", id);

        employeePositionMapper.delete(queryWrapper);

        // 表示是新增岗位
        List<PostIdPercent> postIdPercentList = employeeFormDto.getPostIdPercent();
        for (PostIdPercent postIdPercent : postIdPercentList) {
            BigDecimal percent = postIdPercent.getPercent();
            Long postId = postIdPercent.getPostId();
            EmployeePosition employeePosition1 = new EmployeePosition();
            employeePosition1.setEmpId(id);
            employeePosition1.setPositionId(postId);
            employeePosition1.setPosiPercent(percent);
            employeePositionService.save(employeePosition1);
        }

        LocalDateTime[] time = DateTimeUtils.getTheStartAndEndTimeOfMonth();
        // 更改员工的地域
        List<EmpCoefficient> empCoefficientList = empCoefficientService.lambdaQuery().eq(EmpCoefficient::getEmpId, id).between(EmpCoefficient::getCreateTime, time[0], time[1]).list();
        Long regionId = employeeFormDto.getRegionId();
        BigDecimal grade = employeeFormDto.getGrade();
        if (!CollectionUtils.isEmpty(empCoefficientList)) {
            empCoefficientService.lambdaUpdate().set(Objects.nonNull(regionId), EmpCoefficient::getRegionCoefficientId, regionId).set(Objects.nonNull(grade), EmpCoefficient::getPositionCoefficient, grade).between(EmpCoefficient::getCreateTime, time[0], time[1]).eq(EmpCoefficient::getEmpId, id).update();
        }

        return R.success("员工信息更新成功");
    }

    /**
     * 删除员工信息  操作  员工表  员工岗位表  员工绩效表
     * ids 员工id
     */

    @Override
    @Transactional
    public R deleteEmployeeById(List<Long> ids) {

        ids.forEach(id -> {
            // 首先删除员工表中的信息 不能实际删除，将state 设为 0 视为 删除
            LambdaUpdateWrapper<Employee> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Employee::getState, 0).eq(Employee::getId, id);
            update(updateWrapper);
            // 然后删除员工岗位表的信息
            LambdaQueryWrapper<EmployeePosition> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EmployeePosition::getEmpId, id);
            employeePositionService.remove(wrapper);
            // 最后再删除员工绩效表中的信息 只能删除当月的绩效
            LambdaQueryWrapper<EmpCoefficient> wrapper1 = new LambdaQueryWrapper<>();
            LocalDateTime[] time = DateTimeUtils.getTheStartAndEndTimeOfMonth();
            // 获取员工绩效表的更新时间    员工表与员工绩效表的关系  一对一
            wrapper1.eq(EmpCoefficient::getEmpId, id).between(EmpCoefficient::getUpdateTime, time[0], time[1]);
            empCoefficientService.remove(wrapper1);

            LambdaUpdateWrapper<PositionAssessor> deleteWapper1=new LambdaUpdateWrapper<>();
            deleteWapper1.set(PositionAssessor::getFourthAssessorId,null)
                    .eq(PositionAssessor::getFourthAssessorId,id)
                    .between(PositionAssessor::getUpdateTime, time[0], time[1]);
            PositionAssessorService.update(deleteWapper1);
            LambdaUpdateWrapper<PositionAssessor> deleteWapper2=new LambdaUpdateWrapper<>();
            deleteWapper2.set(PositionAssessor::getThirdAssessorId,null)
                    .eq(PositionAssessor::getThirdAssessorId,id)
                    .between(PositionAssessor::getUpdateTime, time[0], time[1]);
            PositionAssessorService.update(deleteWapper2);
            LambdaUpdateWrapper<PositionAssessor> deleteWapper3=new LambdaUpdateWrapper<>();
            deleteWapper3.set(PositionAssessor::getSecondAssessorId,null)
                    .eq(PositionAssessor::getSecondAssessorId,id)
                    .between(PositionAssessor::getUpdateTime, time[0], time[1]);
            PositionAssessorService.update(deleteWapper3);

            LambdaQueryWrapper<ScoreAssessors> deleteWapper5=new LambdaQueryWrapper<>();
            deleteWapper5.eq(ScoreAssessors::getAssessorId,id)
                    .between(ScoreAssessors::getUpdateTime, time[0], time[1]);
            ScoreAssessorsService.remove(deleteWapper5);

            LambdaQueryWrapper<EmpKpi> deleteWapper6=new LambdaQueryWrapper<>();
            deleteWapper6.eq(EmpKpi::getEmpId,id)
                    .between(EmpKpi::getUpdateTime, time[0], time[1]);
            EmpKpiService.remove(deleteWapper6);
            LambdaQueryWrapper<EmpPiece> deleteWapper7=new LambdaQueryWrapper<>();
            deleteWapper7.eq(EmpPiece::getEmpId,id)
                    .between(EmpPiece::getUpdateTime, time[0], time[1]);
            EmpPieceService.remove(deleteWapper7);
            LambdaQueryWrapper<EmpReward> deleteWapper8=new LambdaQueryWrapper<>();
            deleteWapper8.eq(EmpReward::getEmpId,id)
                    .between(EmpReward::getUpdateTime, time[0], time[1]);
            EmpRewardService.remove(deleteWapper8);
            LambdaQueryWrapper<EmpScore> deleteWapper9=new LambdaQueryWrapper<>();
            deleteWapper9.eq(EmpScore::getEmpId,id)
                    .between(EmpScore::getUpdateTime, time[0], time[1]);
            EmpScoreService.remove(deleteWapper9);
            LambdaQueryWrapper<EmpOkr> deleteWapper10=new LambdaQueryWrapper<>();
            deleteWapper10.eq(EmpOkr::getEmpId,id)
                    .between(EmpOkr::getUpdateTime, time[0], time[1]);
            EmpOkrService.remove(deleteWapper10);
        });
        return R.success("删除成功");
    }

    /**
     * 模糊查询员工信息  操作 员工表
     */
    @Override
    public R lookByLike(EmployeeFormDto employeeFormDto) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getNum, employeeFormDto.getNum()).like(Employee::getName, employeeFormDto.getName());
        Page<Employee> page = new Page<>(employeeFormDto.getPage(), employeeFormDto.getPageSize());
        page(page, wrapper);
        List<Employee> records = page.getRecords();
        if (records == null || records.isEmpty()) {
            page(page);
        }
        return R.success(page);
    }

    /**
     * @param roleId 员工角色id
     * @return 按钮权限
     */
    @Override
    public R router(Long roleId) {
        RouterAndButtonVo routerAndButtonVo = new RouterAndButtonVo();
        // 然后根据角色id去角色按钮表查询按钮id集合
        List<Long> idList = roleBtnService.lambdaQuery().eq(RoleBtn::getRoleId, roleId).list().stream().map(RoleBtn::getBtnId).collect(Collectors.toList());
        // 再根据按钮id去按钮表查询所有的按钮权限
        List<Button> buttons = buttonService.listByIds(idList);
        ArrayList<String> buttonCode = buttons.stream().map(Button::getName).distinct().collect(Collectors.toCollection(ArrayList::new));
        routerAndButtonVo.setButtonCode(buttonCode);
        return R.success(routerAndButtonVo);
    }

   /**
    * @param deptId 部门id
    * @return 员工信息集合
    */
    @Override
    public List<EmployeeVo> getEmployeeVoListByDeptId(Long deptId) {
        ArrayList<EmployeeVo> employeeVos = new ArrayList<>();
        // 获取员工基本信息
        List<Employee> employeeList = getEmployeeListByDeptId(deptId);

        wrapperToEmployeeVo(employeeList, employeeVos);

        return employeeVos;
    }

}




