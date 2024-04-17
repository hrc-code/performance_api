package com.example.workflow.vo;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 黄历
 * @since 2024-03-23
 */
@Data
public class EmpKpiExcel {
    @Alias("序列")
    private Long id;
    @Alias("员工工号")
    private Long empId;
    @Alias("kpi项目名")
    private String name;
    @Alias("kpi条目一")
    private String target1;
    @Alias("kpi条目二")
    private String target2;
    @Alias("条目一输入值")
    private BigDecimal inTarget1;
    @Alias("条目二输入值")
    private BigDecimal inTarget2;
}
