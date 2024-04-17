package com.example.workflow.utils;

import java.util.Map;

public class Find {
    /**
     * 找寻员工的deptId1的整个部门路径是否存在查询条件的deptId
     * @param deptHierarchyMap
     * @param deptId 查询条件
     * @param deptId1 员工所属部门id
     * @return boolean
     */
    public static boolean findPathDeptId(Map<Long,Long> deptHierarchyMap,Long deptId,Long deptId1) {
        if(deptHierarchyMap.get(deptId1) == deptId || deptId == deptId1) return true;
        else if (deptHierarchyMap.get(deptId1) == null) return false;

        return findPathDeptId(deptHierarchyMap,deptId,deptHierarchyMap.get(deptId1));
    }
}
