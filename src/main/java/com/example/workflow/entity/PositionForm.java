package com.example.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class PositionForm {
        @TableId(value ="id",type = IdType.AUTO)
        @JsonSerialize(using= ToStringSerializer.class)
        private Long id;
        private String position;
        @JsonSerialize(using= ToStringSerializer.class)
        private Long deptId;
        /*private List<Long> personList;*/
        private Integer positionType;
        private Boolean state;
        private String ins;

}
