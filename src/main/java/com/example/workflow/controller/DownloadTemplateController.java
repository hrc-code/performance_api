package com.example.workflow.controller;

import com.example.workflow.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

@Slf4j
@RestController
@RequestMapping("/DownloadTemplate")
public class DownloadTemplateController {

    @PostMapping("/positionScore")
    public R<String> positionScore(HttpServletResponse response) throws Exception {
        download(response,"excel/positionScore.xlsx");

        return R.success("岗位-评分导入表");
    }

    @PostMapping("/positionPiece")
    public R<String> positionPiece(HttpServletResponse response) throws Exception {
        download(response,"excel/positionPiece.xlsx");

        return R.success("岗位-计件导入表");
    }

    @PostMapping ("/position")
    public R<String> position(HttpServletResponse response) throws Exception {
        download(response,"excel/position.xlsx");

        return R.success("岗位导入表");
    }

    @PostMapping("/score")
    public R<String> score(HttpServletResponse response) throws Exception {
        download(response,"excel/score.xlsx");

        return R.success("评分条目导入表");
    }

    @PostMapping("/piece")
    public R<String> piece(HttpServletResponse response) throws Exception {
        download(response,"excel/peice.xlsx");

        return R.success("计件条目导入表");
    }

    @PostMapping("/employee")
    public R<String> employee(HttpServletResponse response) throws Exception {
        download(response,"excel/employee.xlsx");

        return R.success("人员导入表");
    }

    @PostMapping("/reward")
    public R<String> reward(HttpServletResponse response) throws Exception {
        download(response,"excel/reward.xlsx");

        return R.success("员工上月特殊绩效导入表");
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
