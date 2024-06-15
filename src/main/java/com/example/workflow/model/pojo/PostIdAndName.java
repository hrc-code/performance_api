package com.example.workflow.model.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 *  所属岗位id和所属岗位名称
 */
@Data
public class PostIdAndName {
    @JsonFormat(shape =JsonFormat.Shape.STRING )
    private Long id;
    private  String position;
}
