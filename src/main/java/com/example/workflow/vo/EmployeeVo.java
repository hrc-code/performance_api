package com.example.workflow.vo;

import com.example.workflow.pojo.DeptIdAndNape;
import com.example.workflow.pojo.PostIdAndName;
import com.example.workflow.pojo.PostIdPercent;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EmployeeVo {

    /**
     *  所属岗位id和所属岗位名称
     */
    private List<PostIdAndName> posts;
    //员工所属角色id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;
    /**
     * 部门名称集合
     **/
    private List<String> deptNames;

    /**
     * 部门iid集合*/
    @JsonSerialize(using = ToStringSerializer.class)
    private List<Long> deptIds;

    /**
     *将一个人多个岗位的部门属性封装起来*/
    private List<DeptIdAndNape> deptIdAndNapeList;

    /**
     *  ceo 级别*/
    private Integer ceoLevel;

    /**
     *  员工id*/
    @JsonFormat(shape =JsonFormat.Shape.STRING )
    private Long id;
    /**
     * 工号
     */
    private String num;
    /**
     * 状态
     */
    private Integer state;
    /**
     * 员工姓名
     */
    private String name;

    /**
     * 级别
     */
    private BigDecimal grade;

    /**
     * 联系电话1
     */
    private String phoneNum1;

    /**
     * 联系电话2
     */
    private String phoneNum2;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 身份证号码
     */
    private String idNum;

    /**
     * 出生年月
     */
    private String birthday;

    /**
     * 通信地址
     */
    private String address;
    /**
     * 所属部门id
     */
    @JsonFormat(shape =JsonFormat.Shape.STRING )
    private Long deptId;

    /**
     * 所属部门名称
     */
    private String deptName;
    /**
     * 所属岗位id(可能有多个值)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private List<Long> postId;

    /**
     * 所属岗位名称
     */
    private List<String> postName;
    /**
     * 所属权限名称
     */
    private String roleName;
    /**
     * 所属地域id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long regionId;

    /**
     * 所属地域名称
     */
    private String regionName;
    /**
     * 备注
     */
    private String remark;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;

    /**
     * 岗位绩效*/
    private List<PostIdPercent> postIdPercent;

}
