package com.example.workflow.base;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

@Data
public class BaseUser {
    @Alias("用户名")
    private String username;
    @Alias("工号")
    private String num;
    @Alias("姓名")
    private String name;
    @Alias("角色名")
    private String roleName;
    @Alias("三级部门")
    private String thirdName;
    @Alias("四级部门")
    private String fourthName;
    @Alias("状态")
    private Integer status;
    @Alias("备注")
    private String remark;
}
