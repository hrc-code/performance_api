package com.example.workflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class PostListVo {
    @JsonFormat(shape =JsonFormat.Shape.STRING )
    private Long id;

    private String position;
}