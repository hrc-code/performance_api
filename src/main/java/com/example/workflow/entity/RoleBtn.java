package com.example.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author 黄历
 * @since 2024-03-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("role_btn")
public class RoleBtn implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 按钮id
     */
    private Long btnId;


}
