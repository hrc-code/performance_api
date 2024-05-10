package com.example.workflow.content.excel;

import com.example.workflow.pojo.EmployeeExcelMsg;

import java.util.List;

/** 上传员工信息上下文*/
public class EmployeeExcelUploadContent {
    private static final ThreadLocal<List<EmployeeExcelMsg>> errorEmployeeList = new ThreadLocal<>();

    public static void setErrorEmployeeList(List<EmployeeExcelMsg> errorEmployeeLists) {
        errorEmployeeList.set(errorEmployeeLists);
    }

    public static List<EmployeeExcelMsg> getErrorEmployeeList() {
        return errorEmployeeList.get();
    }

    public static void clearErrorEmployeeList() {
        errorEmployeeList.remove();
    }
}
