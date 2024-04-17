package com.example.workflow.pojo;

import lombok.Data;

import java.util.List;
@Data
public class DeptIdAndNape {
    /** 部门id*/
   private  List<Long>  deptIds;
   /**
    * 部门名称  拼成字符串，用逗号间隔*/
   private  String deptName;
}
