package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.example.workflow.common.R;
import com.example.workflow.listener.EmployeeRewardExcelReadListener;
import com.example.workflow.pojo.EmployeeRewardExcel;
import com.example.workflow.service.EmpRewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RestController
@RequestMapping("/auth/excel")
public class ExcelController {
    @Autowired
    private EmpRewardService empRewardService;

    /**
     * 导入员工reward excel*/
   @PostMapping("/employeeReward/upload")
    public R importEmployeeReward(MultipartFile file) throws IOException {
       EasyExcel.read(file.getInputStream(), EmployeeRewardExcel.class, new EmployeeRewardExcelReadListener()).sheet().doRead();
       return R.success();
   }
   /** 将EmployeeReward导出为 Excel*/
   @GetMapping("/employeeReward/download")
    public void downloadEmployeeRewardExcel(HttpServletResponse response)   {
       response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
       response.setCharacterEncoding("utf-8");
       String fileName = null;
       try {
           fileName = URLEncoder.encode("employeeReward", "UTF-8").replaceAll("\\+", "%20");
       } catch (UnsupportedEncodingException e) {
           throw new RuntimeException(e);
       }
       response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
       try {
           EasyExcel.write(response.getOutputStream(), EmployeeRewardExcel.class).sheet("模板").doWrite(empRewardService.getAllEmployeeRewardExcel());
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
   }
}
