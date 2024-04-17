package com.example.workflow.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuVo {
    /**
     * 路由或按钮的id
     */
    private String id;
    /**
     * 路由或按钮名称
     */
    private String label;
    /**
     * 类别：【1】目录 【2】菜单 【3】按钮
     */
    private Short type;
    /**
     * 子集
     */
    private List<MenuVo> children;
}
