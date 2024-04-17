package com.example.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 按钮表
 * </p>
 *
 * @author 黄历
 * @since 2024-03-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("button")
public class Button implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 按钮权限字符
     */
    private String name;

    /**
     * 父路由id
     */
    private String parentRouter;


}
