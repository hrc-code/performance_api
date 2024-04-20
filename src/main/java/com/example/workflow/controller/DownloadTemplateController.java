package com.example.workflow.controller;

import com.example.workflow.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Slf4j
@RestController
@RequestMapping("/DownloadTemplate")
public class DownloadTemplateController {

    @RequestMapping("/positionScore")
    public R positionScore(HttpServletResponse response) throws Exception {
        download(response,"excel/positionScore.xlsx");

        return R.success();
    }

    @RequestMapping("/position")
    public R position(HttpServletResponse response) throws Exception {
        download(response,"excel/position.xlsx");

        return R.success();
    }

    @RequestMapping("/score")
    public R score(HttpServletResponse response) throws Exception {
        download(response,"excel/score.xlsx");

        return R.success();
    }

    @RequestMapping("/piece")
    public R piece(HttpServletResponse response) throws Exception {
        download(response,"excel/peice.xlsx");

        return R.success();
    }

    @RequestMapping("/employee")
    public R employee(HttpServletResponse response) throws Exception {
        download(response,"excel/employee.xlsx");

        return R.success();
    }

    public void download(HttpServletResponse response,String excel) throws Exception{

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=import.xlsx");
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(excel);

        ServletOutputStream outputStream = response.getOutputStream();
        byte[] bytes = new byte[1024];
        while (resourceAsStream.read(bytes) != -1) {
            outputStream.write(bytes);
        }
        outputStream.close();
        resourceAsStream.close();
    }
}
