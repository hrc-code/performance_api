package com.example.workflow.dto;

import com.example.workflow.pojo.PostIdPercent;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EmployeeFormDto {

    /**
     * 角色id*/
    private  Long roleId;

    /**
     * 部门绩效*/
    private List<PostIdPercent> postIdPercent;
    /** 员工id*/
    private Long id;

    /**
     * 工号
     */
    private String num;

    /**
     * 员工姓名
     */
    private String name;

    /**
     * 出生年月
     */
    private String birthday;

    /**
     * 身份证号码
     */
    private String idNum;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     *  所属岗位id(可能有多个值)
     */
    private List<Long> postId;

    /**
     *岗位绩效
     */
    private BigDecimal grade;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 联系电话1
     */
    private String phoneNum1;

    /**
     * 联系电话2
     */
    private String phoneNum2;

    /**
     * 通信地址
     */
    private String address;

    /**
     * 地域系数id
     */
    private Long regionId;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 备注
     */
    private String remark;


    /**
     *  分页*/
    private Integer page;

    /**
     * 每次分页的大小*/
    private Integer pageSize;
}
