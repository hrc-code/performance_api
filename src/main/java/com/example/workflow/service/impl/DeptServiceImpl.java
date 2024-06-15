package com.example.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.common.R;
import com.example.workflow.mapper.DeptHierarchyMapper;
import com.example.workflow.mapper.DeptMapper;
import com.example.workflow.model.dto.DeptFormDto;
import com.example.workflow.model.entity.Dept;
import com.example.workflow.model.entity.DeptHierarchy;
import com.example.workflow.model.entity.Employee;
import com.example.workflow.model.entity.EmployeePosition;
import com.example.workflow.model.entity.Position;
import com.example.workflow.model.vo.DeptVo;
import com.example.workflow.model.vo.EmployeeVo;
import com.example.workflow.model.vo.PostListVo;
import com.example.workflow.service.DeptHierarchyService;
import com.example.workflow.service.DeptService;
import com.example.workflow.service.EmployeePositionService;
import com.example.workflow.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门 服务实现类
 * </p>
 *
 * @author 黄历
 * @since 2024-03-12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept>
        implements DeptService {

    private final DeptHierarchyMapper deptHierarchyMapper;

    private final DeptHierarchyService deptHierarchyService;

    private final PositionService positionService;

    private final EmployeePositionService employeePositionService;

    @Autowired
    private DeptService DeptService;

    /**
     * 操作 部门表  部门继承表
     * 获得这样格式部门名  中捷总公司/深圳分公司/业务一部
     */
    @Override
    public Map<Long, String> getDeptNameMap(List<Long> ids) {
        // 首先根据部门id获取部门
        List<Dept> depts = lambdaQuery().in(Dept::getId, ids).list();
        if (CollectionUtils.isEmpty(depts)) {
            // 没有对应的部门
            return Collections.emptyMap();
        }
        // 根据部门id分组后的部门name
        Map<Long, String> deptNameMap = depts.stream().filter(Objects::nonNull).collect(Collectors.toMap(Dept::getId, Dept::getDeptName));
        // 查询全部的上一级与本级别的记录
        List<DeptHierarchy> oneDeptHierarchies = deptHierarchyService.lambdaQuery().in(DeptHierarchy::getChildId, ids).list();
        if (CollectionUtils.isEmpty(oneDeptHierarchies)) {
            // 无上一级部门
            return deptNameMap;
        }
        // key 本部门id  value 上一级部门id
        HashMap<Long, Long> oneIdMap = new HashMap<>();
        // 获取上一级部门id
        Set<Long> oneParentIdSet = oneDeptHierarchies.stream().filter(Objects::nonNull).map(deptHierarchy -> {
            Long parentId = deptHierarchy.getParentId();
            Long childId = deptHierarchy.getChildId();
            oneIdMap.put(childId, parentId);
            return parentId;
        }).collect(Collectors.toSet());
        // 获取全部的上一级部门
        List<Dept> oneDept = lambdaQuery().in(Dept::getId, oneParentIdSet).list();
        // 根据上一级部门id分组上一级部门name
        Map<Long, String> oneDeptNameMap = oneDept.stream().filter(Objects::nonNull).collect(Collectors.toMap(Dept::getId, Dept::getDeptName));

        // 将本部门名与上一级部门名结合
        ids.stream().filter(Objects::nonNull).forEach(deptId -> {
            Long oneDeptId = oneIdMap.get(deptId);
            String oneName = oneDeptNameMap.get(oneDeptId);
            String deptName = deptNameMap.get(deptId);
            if (oneName != null) {
                deptNameMap.put(deptId, oneName + "/" + deptName);
            }
        });

        // 获取上一级与上二级的记录
        List<DeptHierarchy> twoDeptHierarchies = deptHierarchyService.lambdaQuery().in(DeptHierarchy::getChildId, oneParentIdSet).list();
        if (CollectionUtils.isEmpty(twoDeptHierarchies)) {
            // 没有上二级
            return deptNameMap;
        }

        // 获取上部上二级部门id
        HashMap<Long, Long> twoIdMap = new HashMap<>();
        Set<Long> twoIdSet = twoDeptHierarchies.stream().filter(Objects::nonNull).map(deptHierarchy -> {
            Long parentId = deptHierarchy.getParentId();
            Long childId = deptHierarchy.getChildId();
            twoIdMap.put(childId, parentId);
            return parentId;
        }).collect(Collectors.toSet());
        // 获取全部上二级部门
        List<Dept> twoDeptList = lambdaQuery().in(Dept::getId, twoIdSet).list();
        // 根据部门id分组之后的部门name
        Map<Long, String> twoDeptNameMap = twoDeptList.stream().filter(Objects::nonNull).collect(Collectors.toMap(Dept::getId, Dept::getDeptName));

        // 将本部门name与上一级name与上二级name结合
        ids.stream().filter(Objects::nonNull).forEach(deptId -> {
            Long oneDeptId = oneIdMap.get(deptId);
            Long twoDeptId = twoIdMap.get(oneDeptId);
            if (twoDeptId != null) {
                // 本部name与一级name
                String deptName = deptNameMap.get(deptId);
                String twoDeptName = twoDeptNameMap.get(twoDeptId);
                deptNameMap.put(deptId, twoDeptName + "/" + deptName);
            }
        });
        // 获取上二级与上三级部门的记录
        List<DeptHierarchy> threeDeptHierarchies = deptHierarchyService.lambdaQuery().in(DeptHierarchy::getChildId, twoIdSet).list();
        if (CollectionUtils.isEmpty(threeDeptHierarchies)) {
            // 没有上三级
            return deptNameMap;
        }
        // 获取全部三级部门id  twoId threeId
        HashMap<Long, Long> threeIdMap = new HashMap<>();
        Set<Long> threeIdSet = threeDeptHierarchies.stream().filter(Objects::nonNull).map(deptHierarchy -> {
            Long childId = deptHierarchy.getChildId();
            Long parentId = deptHierarchy.getParentId();
            threeIdMap.put(childId, parentId);
            return parentId;
        }).collect(Collectors.toSet());
        // 获取全部三级部门
        List<Dept> threeDept = lambdaQuery().in(Dept::getId, threeIdSet).list();
        // 根据部门id分组部门name
        Map<Long, String> threeDeptNameMap = threeDept.stream().filter(Objects::nonNull).collect(Collectors.toMap(Dept::getId, Dept::getDeptName));
        // 将本部门name与一级，二级，三级拼接
        ids.stream().filter(Objects::nonNull).forEach(deptId -> {
            Long oneDeptId = oneIdMap.get(deptId);
            Long twoDeptId = twoIdMap.get(oneDeptId);
            Long threeDeptId = threeIdMap.get(twoDeptId);
            if (threeDeptId != null) {
                String threeDeptName = threeDeptNameMap.get(threeDeptId);
                if (threeDeptName != null) {
                    deptNameMap.compute(deptId, (k, deptName) -> threeDeptName + "/" + deptName);
                }
            }
        });
        return deptNameMap;
    }

    @Override
    public List<DeptVo> getDeptTree(Integer id) {
        if (id == 5) {
            id = 4;
        }
        // 查询到的部门列表
        List<Dept> dept = Db.lambdaQuery(Dept.class)
                .le(id != null, Dept::getLevel, id)
                .list();
        // 初始化部门树列表
        List<DeptVo> deptVos = BeanUtil.copyToList(dept, DeptVo.class);

        Map<Long, DeptVo> deptMap = new HashMap<>();
        for (DeptVo deptVo : deptVos) {
            deptVo.setChildren(new ArrayList<>()); // 初始化子部门列表
            deptMap.put(deptVo.getId(), deptVo);
        }

        // 获取部门层级关系
        List<DeptHierarchy> deptHierarchyList = Db.lambdaQuery(DeptHierarchy.class).list();

        Set<Long> deptHierarchySet = new HashSet<>();

        for (DeptHierarchy dh : deptHierarchyList) {
            deptHierarchySet.add(dh.getChildId());

            DeptVo parentDept = deptMap.get(dh.getParentId());
            DeptVo childDept = deptMap.get(dh.getChildId());

            if (parentDept != null && childDept != null) {
                parentDept.getChildren().add(childDept);
            }
        }

        // 找到根部门,对deptList过滤
        List<DeptVo> rootDepths = new ArrayList<>();
        for (DeptVo deptVo : deptVos) {
            if (!deptHierarchySet.contains(deptVo.getId())) {
                // 如果部门没有父部门，则该部门为根部门
                rootDepths.add(deptVo);
            }
        }

        return rootDepths;
    }

    /**
     * 增加部门  操作部门表  部门继承表
     * deptFormDto 部门表单信息
     */
    @Override
    public Boolean addDept(DeptFormDto deptFormDto) {
        Dept dept = new Dept();
        BeanUtil.copyProperties(deptFormDto, dept);
        int level = deptFormDto.getParentLevel() + 1;
        dept.setLevel(level);
        // 查询同级部门是否存在同名的 where level = ? and  dept_name = ?
        LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dept::getDeptName, dept.getDeptName());
        Dept one = DeptService.getOne(queryWrapper);

        if (one == null) {
            save(dept);
            DeptHierarchy deptHierarchy = new DeptHierarchy();
            deptHierarchy.setParentId(deptFormDto.getParentId());
            deptHierarchy.setChildId(dept.getId());
            Db.save(deptHierarchy);
            return true;
        }
        return false;
    }

    /**
     * 删除部门同时需要删除树形结构的关系   操作 部门表， 部门继承表  员工岗位表   岗位表
     * ids  部门id
     */
    @Transactional
    @Override
    public Boolean deleteDept(List<Long> ids) {

        // 需要删除的部门id集合
        HashSet<Long> deleteDeptIdSet = new HashSet<>();
        // 该部门下的全部岗位没有人员，将属于该部门的全部岗位删除，并且将该部门删除
        // 获取全部的岗位
        List<Position> positionList = positionService.lambdaQuery().in(Position::getDeptId, ids).list();
        // 没有岗位
        if (CollectionUtils.isEmpty(positionList)) {
            // 全部部门都没有岗位
            deleteDeptIdSet.addAll(ids);
        } else {
            // 根据部门id分组的岗位map
            Map<Long, List<Position>> positionsMap = positionList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(Position::getDeptId));
            // key 为部门id value为部门对应的岗位id集合
            HashMap<Long, List<Long>> deptPositionIdMap = new HashMap<>();
            positionsMap.forEach((deptId, positions) -> {
                ArrayList<Long> positionIdList = new ArrayList<>();
                // 如果这部门没有岗位就将它加入要删除的部门
                if (CollectionUtils.isEmpty(positions)) {
                    deleteDeptIdSet.add(deptId);
                } else {
                    // 获取这个部门的全部岗位id
                    Set<Long> positionIdSet = positions.stream().filter(Objects::nonNull).map(Position::getId).collect(Collectors.toSet());
                    positionIdList.addAll(positionIdSet);
                    deptPositionIdMap.put(deptId, positionIdList);
                }
            });

            // 获取全部岗位id
            Set<Long> positionIdSet = positionList.stream().filter(Objects::nonNull).map(Position::getId).collect(Collectors.toSet());
            // 获取全部的员工岗位数据
            List<EmployeePosition> employeePositionsList = employeePositionService.lambdaQuery().in(EmployeePosition::getPositionId, positionIdSet).list();
            if (CollectionUtils.isEmpty(employeePositionsList)) {
                // 有岗位但是岗位都没有员工  将全部部门加入要删除集合中
                deleteDeptIdSet.addAll(ids);
            } else {
                // 根据岗位id分组后的员工
                Map<Long, List<EmployeePosition>> employeeMap = employeePositionsList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(EmployeePosition::getPositionId));
                ids.stream().filter(Objects::nonNull).forEach(deptId -> {
                    // 根据部门id获得这个部门的全部岗位id
                    List<Long> positionIdList = deptPositionIdMap.get(deptId);
                    if (CollectionUtils.isEmpty(positionIdList)) {
                        // 如果这个部门没有岗位就将其加入删除集合
                        deleteDeptIdSet.add(deptId);
                    } else {
                        // 只有当这个部门的全部的岗位都没有人时才可以将其删除
                        int count = 0;
                        for (Long positionId : positionIdList) {
                            // 根据岗位id获得这个岗位的全部人员
                            List<EmployeePosition> employeePositions = employeeMap.get(positionId);
                            // 如果为空则没有人员则说明这个岗位没有员工
                            if (CollectionUtils.isEmpty(employeePositions)) {
                                count++;
                            }
                            // 一个岗位有员工就不能删除
                            break;
                        }
                        // 全部岗位没有员工
                        if (count == positionIdList.size()) {
                            deleteDeptIdSet.add(deptId);
                        }
                    }
                });
            }

        }

        // 有下级部门，下级部门没有人可以将该部门删除
        // 只去获取可以删除的部门全部下级部门
        if (!CollectionUtils.isEmpty(deleteDeptIdSet)) {
            List<DeptHierarchy> deptHierarchies = deptHierarchyService.lambdaQuery().in(DeptHierarchy::getParentId, deleteDeptIdSet).list();
            if (!CollectionUtils.isEmpty(deptHierarchies)) {
                // 根据部门id分组后的全部下级部门
                Map<Long, List<DeptHierarchy>> childMap = deptHierarchies.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(DeptHierarchy::getParentId));
                childMap.forEach((deptId, deptHierarchyList) -> {
                    // 如果这个部门没有下级部门 将它加入删除集合  有下级部门不允许删除
                    if (CollectionUtils.isEmpty(deptHierarchyList)) {
                        deleteDeptIdSet.add(deptId);
                    } else {
                        // 有下级就不允许删除
                        deleteDeptIdSet.remove(deptId);
                    }
                });
            }
            // 在部门表删除
            boolean removed = removeBatchByIds(deleteDeptIdSet);
            // 删除其拥有的岗位但没有人
            LambdaUpdateWrapper<Position> wrapper = new LambdaUpdateWrapper<>();
            if (!CollectionUtils.isEmpty(deleteDeptIdSet)) {
                wrapper.in(Position::getDeptId, deleteDeptIdSet);
                positionService.remove(wrapper);

                // 在部门继承表中删除该部门的继承关系
                LambdaQueryWrapper<DeptHierarchy> wrapper1 = new LambdaQueryWrapper<>();
                wrapper1.in(DeptHierarchy::getChildId, deleteDeptIdSet);
                deptHierarchyService.remove(wrapper1);
            }
            return removed;
        }
        return false;
    }

    @Override
    public DeptFormDto getDeptInfo(Long id) {
        // 要查询的部门信息
        Dept dept = Db.lambdaQuery(Dept.class).eq(Dept::getId, id).one();
        if (dept == null) {
            return null;
        }
        // 要查询的部门的父部门id
        DeptHierarchy deptHierarchy = Db.lambdaQuery(DeptHierarchy.class).eq(DeptHierarchy::getChildId, id).one();
        //   默认为自己   若有父部门则为父部门id
        Long parentId = id;
        if (deptHierarchy != null) {
            parentId = deptHierarchy.getParentId();
        }
        // 要查询的部门的父部门等级  默认为自己  若有父部门则为父部门
        Integer parentLevel = dept.getLevel();
        Dept parentDept = Db.lambdaQuery(Dept.class).eq(Dept::getId, parentId).one();
        if (parentDept != null) {
            parentLevel = parentDept.getLevel();
        }
        DeptFormDto deptFormDto = BeanUtil.copyProperties(dept, DeptFormDto.class);
        deptFormDto.setParentId(parentId);
        deptFormDto.setParentLevel(parentLevel);

        return deptFormDto;
    }


    /**
     * 更新部门信息  操作 部门表
     * deptFormDto 部门表单
     * id 更新部门id
     */
    @Override
    public Boolean updateDept(DeptFormDto deptFormDto, Long id) {
        // 当前部门等级
        Integer level = deptFormDto.getLevel();
        String deptName = deptFormDto.getDeptName();
        // 父部门级别只能和更新前父部门级别相同，例如4级部门的父部门级别为三级更新后也只能为三级
        if (deptFormDto.getParentLevel() + 1 != level) {
            return false;
        }
        if (level >= 5) {
            log.error("更新部门级别 > 4");
            return false;
        }
        // 去部门表查询同级表是否存在同名
        Dept one = Db.lambdaQuery(Dept.class).eq(Dept::getLevel, level)
                .ne(deptName != null, Dept::getDeptName, deptName)
                .eq(Dept::getDeptName, deptName).one();
        if (one != null) {
            log.error("存在同级同名");
            return false;
        } else {
            Dept dept = new Dept();
            BeanUtil.copyProperties(deptFormDto, dept);
            dept.setId(id);
            this.updateById(dept);
            LambdaUpdateWrapper<DeptHierarchy> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper
                    .eq(DeptHierarchy::getChildId, id)
                    .set(DeptHierarchy::getParentId, deptFormDto.getParentId());
            deptHierarchyMapper.update(null, lambdaUpdateWrapper);
        }
        return true;
    }

    /**
     * 获取该部门的全部岗位  操作  岗位表
     * id 部门id
     */

    @Override
    public R getAllPositionsById(Long id) {
        List<Position> positions = positionService.lambdaQuery().eq(Position::getDeptId, id).list();
        if (positions == null || positions.isEmpty()) {
            return R.error("该部门没有岗位");
        }
        List<PostListVo> postListVos = BeanUtil.copyToList(positions, PostListVo.class);
        return R.success(postListVos);
    }

    /**
     * 查询出这个岗位的全部上级  操作  岗位表  部门继承表
     * id  岗位id
     * return 上级与本机部门id 允许null
     */

    @Override
    public R<List<Long>> getAllSuperiorDept(Long id) {
        // 先从岗位表查询出部门id
        Position position = positionService.getById(id);
        if (position == null) {
            return R.error("岗位没有所对应的部门");
        }
        Long deptId = position.getDeptId();
        ArrayList<Long> result = new ArrayList<>(4);
        // 最高四层部门 所以查四次部门继承表
        DeptHierarchy three = deptHierarchyService.lambdaQuery().eq(DeptHierarchy::getChildId, deptId).one();
        if (three == null) {
            result.add(null);
            result.add(null);
            result.add(null);
            result.add(deptId);
        } else {
            DeptHierarchy two = deptHierarchyService.lambdaQuery().eq(DeptHierarchy::getChildId, three.getParentId()).one();
            if (two == null) {
                result.add(null);
                result.add(null);
                result.add(three.getParentId());
                result.add(deptId);
            } else {
                DeptHierarchy one = deptHierarchyService.lambdaQuery().eq(DeptHierarchy::getChildId, two.getParentId()).one();
                if (one == null) {
                    result.add(null);
                    result.add(two.getParentId());
                    result.add(three.getParentId());
                    result.add(deptId);
                } else {
                    result.add(one.getParentId());
                    result.add(two.getParentId());
                    result.add(three.getParentId());
                    result.add(deptId);
                }
            }
        }


        return R.success(result);
    }

    /**
     * 根据父部门id获取这个部门的全部后代id
     */

    public Set<Long> getChildeDeptIdSet(Long parentId) {
        HashSet<Long> deptIdSet = new HashSet<>();
        getChildrenDeptIdSet(parentId, deptIdSet);
        return deptIdSet;
    }

    private void getChildrenDeptIdSet(Long parentId, Set<Long> deptIdSet) {
        if (parentId == null) {
            return;
        }
        List<DeptHierarchy> deptHierarchies = Db.lambdaQuery(DeptHierarchy.class).eq(DeptHierarchy::getParentId, parentId).list();
        if (!CollectionUtils.isEmpty(deptHierarchies)) {
            for (DeptHierarchy deptHierarchy : deptHierarchies) {
                Long childrenId = deptHierarchy.getChildId();
                deptIdSet.add(childrenId);
                getChildrenDeptIdSet(childrenId, deptIdSet);
            }
        }
    }


    /**
     * 获取所有二级ceo、所有三级ceo、所有四级ceo的列表的接口  操作 岗位表， 员工岗位表， 员工表
     * id 为部门级别
     * type < 5 为ceo
     * return  员工id, 员工name ， ceoLevel
     */

    @Override
    public R getAllCeo(Integer id) {
        ArrayList<EmployeeVo> employeeVos = new ArrayList<>();
        // 先根据部门级别去部门表查询出所有这个级别的部门
        List<Dept> depts = lambdaQuery().eq(Dept::getLevel, id).list();
        if (depts == null || depts.isEmpty()) {
            return R.error("没有改数据");
        }
        // 然后根据部门id去岗位表查询这个部门的全部岗位
        depts.stream().filter(Objects::nonNull).forEach(dept -> {
            Long deptId = dept.getId();
            List<Position> positions = positionService.lambdaQuery().eq(Position::getDeptId, deptId).list();
            // 根据type < 5 筛选出ceo
            positions.stream().filter(Objects::nonNull).filter(position -> position.getType() < 5).forEach(position -> {
                // 根据岗位id去员工岗位表查询员工id
                Long positionId = position.getId();
                List<EmployeePosition> employeePositions = employeePositionService.lambdaQuery().eq(EmployeePosition::getPositionId, positionId).list();
                employeePositions.stream().filter(Objects::nonNull).forEach(employeePosition -> {
                    Long empId = employeePosition.getEmpId();
                    // 根据员工id全部员工表查询员工name
                    Employee employee = Db.getById(empId, Employee.class);
                    if (employee != null) {
                        String name = employee.getName();
                        EmployeeVo employeeVo = new EmployeeVo();
                        employeeVo.setCeoLevel(id);
                        employeeVo.setName(name);
                        employeeVo.setId(empId);
                        employeeVos.add(employeeVo);
                    }
                });
            });
        });

        return R.success(employeeVos);
    }

    /**
     * 操作 部门表  部门继承表
     * 获得这样格式部门名  中捷总公司/深圳分公司/业务一部
     */
    @Override
    public String getDeptName(Long id) {
        Dept oneDept = getById(id);
        String oneName = oneDept.getDeptName();
        Long oneId = oneDept.getId();
        DeptHierarchy parentOne = Db.lambdaQuery(DeptHierarchy.class).eq(DeptHierarchy::getChildId, oneId).one();
        if (parentOne == null) {
            return oneName;
        } else {
            Long twoId = parentOne.getParentId();
            Dept twoDept = getById(twoId);
            String twoName = twoDept.getDeptName();
            DeptHierarchy parentTwo = Db.lambdaQuery(DeptHierarchy.class).eq(DeptHierarchy::getChildId, twoId).one();
            if (parentTwo == null) {
                return twoName + "/" + oneName;
            } else {
                Long threeId = parentTwo.getParentId();
                Dept threeDept = getById(threeId);
                String threeName = threeDept.getDeptName();
                DeptHierarchy parentThree = Db.lambdaQuery(DeptHierarchy.class).eq(DeptHierarchy::getChildId, threeId).one();
                if (parentThree == null) {
                    return threeName + "/" + twoName + "/" + oneName;
                } else {
                    Long fourthId = parentThree.getId();
                    Dept fourthDept = getById(fourthId);
                    String fourthName = fourthDept.getDeptName();
                    return fourthName + "/" + threeName + "/" + twoName + "/" + oneName;
                }

            }
        }
    }
}
