package com.example.workflow.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PostIdPercent {
    /**
     * 所属岗位id
     */
    @JsonFormat(shape =JsonFormat.Shape.STRING )
    private Long postId;
    /**
     * 所选岗位占的百分比
     */
    private BigDecimal percent;
}
