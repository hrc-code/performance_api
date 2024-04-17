package com.example.workflow.vo;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

@Data
public class UserError {
    private String num;
    private String name;
    private String phoneNum;
    private String roleName;
    private String deptName;
    private Integer state;
    private String remark;
    /**
     * 错误原因
     */
    private String cause;
}
