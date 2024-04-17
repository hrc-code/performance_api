package com.example.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PositionView {
    @JsonSerialize(using= ToStringSerializer.class)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String position;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long deptId;
    private String deptName;
    private Short type;
    private String typeName;
    private Short auditStatus;
    private Short state;
    private String ins;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime updateTime;
}
