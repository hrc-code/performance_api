package com.example.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.workflow.common.R;
import com.example.workflow.dto.DeptFormDto;
import com.example.workflow.entity.Dept;
import com.example.workflow.vo.DeptVo;

import java.util.List;
import java.util.Map;

/**
* @author hrc
* @description 针对表【dept(部门)】的数据库操作Service
* @createDate 2024-03-27 21:39:56
*/
public interface DeptService extends IService<Dept> {
    Map<Long, String> getDeptNameMap(List<Long> ids);

    List<DeptVo> getDeptTree(Integer id);

    Boolean addDept(DeptFormDto deptFormDto);

    Boolean deleteDept(List<Long> ids);

    DeptFormDto getDeptInfo(Long id);

    Boolean updateDept(DeptFormDto deptFormDto, Long id);

     R getAllPositionsById(Long id);

     R<List<Long>> getAllSuperiorDept(Long id);

     R getAllCeo(Integer id);
     /**
      * 中捷总公司/深圳分公司/业务一部*/
     String getDeptName(Long id);
}
