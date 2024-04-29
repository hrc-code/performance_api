package com.example.workflow.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.workflow.common.R;
import com.example.workflow.entity.EmpWage;
import com.example.workflow.listener.EmployeeExcelReadListener;
import com.example.workflow.listener.EmployeeRewardExcelReadListener;
import com.example.workflow.pojo.EmployeeExcel;
import com.example.workflow.pojo.EmployeeRewardExcel;
import com.example.workflow.service.EmpRewardService;
import com.example.workflow.service.EmpWageService;
import com.example.workflow.service.EmployeeCoefficientService;
import com.example.workflow.service.EmployeeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/auth/excel")
public class ExcelController {

    private final EmployeeService employeeService;
    private final EmpRewardService empRewardService;
    private final EmpWageService EmpWageService;
    private final EmployeeCoefficientService EmployeeCoefficientService;

    LocalDate today = LocalDate.now();
    LocalDateTime beginTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
    LocalDateTime endTime = LocalDateTime.of(today.withDayOfMonth(today.lengthOfMonth()), LocalTime.MAX);

    public ExcelController(EmployeeService employeeService, EmpRewardService empRewardService, EmpWageService EmpWageService, EmployeeCoefficientService EmployeeCoefficientService) {
        this.employeeService = employeeService;
        this.empRewardService = empRewardService;
        this.EmpWageService = EmpWageService;
        this.EmployeeCoefficientService = EmployeeCoefficientService;
    }

    /**
     * 导入员工reward excel*/
   @PostMapping("/employeeReward/upload")
    public R<String> importEmployeeReward(MultipartFile file) throws IOException {
       EasyExcel.read(file.getInputStream(), EmployeeRewardExcel.class, new EmployeeRewardExcelReadListener()).sheet().doRead();

       List<EmpWage> list= EmpWageService.lambdaQuery()
               .eq(EmpWage::getState,1)
               .apply(StringUtils.checkValNotNull(beginTime),
                       "date_format (create_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
               .apply(StringUtils.checkValNotNull(endTime),
                       "date_format (create_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                       .list();
       list.forEach(x-> EmployeeCoefficientService.fileOne(x.getEmpId(),x.getPositionId()));
       return R.success();
   }
   /** 将EmployeeReward导出为 Excel*/
   @GetMapping("/employeeReward/download")
    public void downloadEmployeeRewardExcel(HttpServletResponse response)   {
       response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
       response.setCharacterEncoding("utf-8");
       String fileName;
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

    /** 导入员工基本信息*/
    @PostMapping("/employee/upload")
    public R<String> importEmployee(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), EmployeeExcel.class, new EmployeeExcelReadListener()).sheet().doRead();
        return R.success();
    }

    /**
     * 导出员工信息*/
    @GetMapping("/employee/download")
    public void downloadEmployeeExcel(HttpServletResponse response)   {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName;
        try {
            fileName = URLEncoder.encode("employee", "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        try {
            EasyExcel.write(response.getOutputStream(), EmployeeExcel.class).sheet("模板").doWrite(employeeService.getEmployeeExcels());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
