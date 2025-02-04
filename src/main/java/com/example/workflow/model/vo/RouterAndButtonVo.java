package com.example.workflow.model.vo;

import com.example.workflow.model.entity.Router;
import lombok.Data;

import java.util.List;

@Data
public class RouterAndButtonVo {
    /** 一个员工的全部路由信息*/
   private List<Router> routerList;
   /**
    * 按钮权限字符*/
    List<String> buttonCode;
}