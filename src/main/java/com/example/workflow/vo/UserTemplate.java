package com.example.workflow.vo;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

@Data
public class UserTemplate {
    @Alias("工号")
    private String num;
    @Alias("姓名")
    private String name;
    @Alias("手机号")
    private String phoneNum;
    @Alias("角色名")
    private String roleName;
    @Alias("部门")
    private String deptName;
    @Alias("状态")
    private Integer state;
    @Alias("备注")
    private String remark;
}
