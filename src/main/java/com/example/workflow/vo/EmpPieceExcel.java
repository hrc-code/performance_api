package com.example.workflow.vo;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
public class EmpPieceExcel implements Serializable {
    @Alias("序列")
    private Long id;
    @Alias("员工工号")
    private Long empId;
    @Alias("计件条目")
    private String item;
    @Alias("工单号")
    private String workOrder;
}
