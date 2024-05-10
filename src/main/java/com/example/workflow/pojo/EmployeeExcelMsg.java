package com.example.workflow.pojo;

import lombok.Data;

@Data
public class EmployeeExcelMsg {
    private String msg;

    @Override
    public String toString() {
        return msg + "\n";
    }
}
