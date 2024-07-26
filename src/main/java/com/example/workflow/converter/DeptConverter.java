package com.example.workflow.converter;

import com.example.workflow.model.entity.Dept;
import com.example.workflow.model.vo.DeptVo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeptConverter {

    List<DeptVo> pos2vos(List<Dept> dept);

    DeptVo po2vo(Dept dept);

}
