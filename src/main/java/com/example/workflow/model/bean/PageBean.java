package com.example.workflow.model.bean;

import lombok.Data;

import java.util.List;

@Data
public class PageBean {
    private Long count;
    private List rows;

    public PageBean(long total, List result) {
        this.count = total;
        this.rows = result;
    }
}
