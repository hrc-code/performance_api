package com.example.workflow.vo;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserExcel {
    @Alias("序号")
    private Long id;
    @Alias("工号")
    private String num;
    @Alias("姓名")
    private String name;
    @Alias("手机")
    private String phoneNum;
    @Alias("角色")
    private String roleName;
    @Alias("部门")
    private String deptName;
    @Alias("状态")
    private Integer state;
    @Alias("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Alias("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @Alias("备注")
    private String remark;
}
