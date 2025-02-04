package com.example.workflow.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.common.R;
import com.example.workflow.model.bean.PageBean;
import com.example.workflow.model.entity.PdfFile;
import com.example.workflow.service.PdfFileService;
import com.example.workflow.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * pdf控制器
 *
 * @author 黄历
 * @since 2024-03-14
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pdf")
public class PdfFilesController {

    private final PdfFileService pdfFileService;


    @GetMapping("/getNowList")
    public R<List<PdfFile>> getNowList() {
        LocalDateTime[] time = DateTimeUtils.getTheStartAndEndTimeOfMonth();
        LocalDateTime beginTime = time[0];
        LocalDateTime endTime = time[1];
        List<PdfFile> list = pdfFileService.lambdaQuery()
                .apply(StringUtils.checkValNotNull(beginTime),
                        "date_format (update_time,'%Y-%m-%d %H:%i:%s') >= date_format ({0},'%Y-%m-%d %H:%i:%s')", beginTime)
                .apply(StringUtils.checkValNotNull(endTime),
                        "date_format (update_time,'%Y-%m-%d %H:%i:%s') <= date_format ({0},'%Y-%m-%d %H:%i:%s')", endTime)
                .list();

        return R.success(list);
    }

    /**
     * 获取依据pdf文件
     *
     * @return List<PdfFile>
     */
    @GetMapping("/list")
    public R<PageBean> getPdfFileList(
            String id,
            String fileName,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<PdfFile> page = new Page<>(pageNum, pageSize);

        Page pdfFiles = Db.lambdaQuery(PdfFile.class)
                .select(PdfFile::getId, PdfFile::getFileName, PdfFile::getUploadTime, PdfFile::getUpdateTime)
                .like(fileName != null, PdfFile::getFileName, fileName)
                .like(id != null, PdfFile::getId, id)
                .page(page);

        PageBean pageBean = new PageBean(pdfFiles.getTotal(), pdfFiles.getRecords());

        return R.success(pageBean);
    }

    /**
     * 删除依据pdf文件
     *
     * @return R
     */
    @DeleteMapping("/delete/{ids}")
    public R deletePdf(@PathVariable List<Long> ids) {

        boolean bool = Db.removeByIds(ids, PdfFile.class);

        if (bool) {
            return R.success();
        }
        return R.error("删除文件失败");
    }

    /**
     * 导入依据pdf文件
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return new ResponseEntity<>("请上传文件", HttpStatus.BAD_REQUEST);
        }

        PdfFile pdfFile = new PdfFile();
        pdfFile.setFileName(file.getOriginalFilename());
        pdfFile.setFileContent(file.getBytes());
        pdfFile.setUploadTime(LocalDateTime.now());
        pdfFile.setId(String.valueOf(System.currentTimeMillis()));

        pdfFileService.save(pdfFile);
        return new ResponseEntity<>("上传文件成功", HttpStatus.OK);

    }

    /**
     * 预览pdf文件
     */
    @GetMapping("/preview/{id}")
    public void showPDF(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream inputStream;

            //获取pdf文件
            PdfFile pdfFile = Db.lambdaQuery(PdfFile.class).eq(id != null,PdfFile::getId,id).one();

            //二进制文件内容
            byte[] content = pdfFile.getFileContent();

            if (null != content && content.length > 0) {
                String pdfName = pdfFile.getFileName();
                //在这里设置头信息
                response.setContentType("application/pdf;charset=UTF-8");
                response.setHeader("Content-Disposition", "inline");
                OutputStream out = response.getOutputStream();
                inputStream = new ByteArrayInputStream(content);
                // 需要引入pdfbox依赖,这里只是为了修改在浏览器上预览时展示的文件名,如果不需要,别的方式输出也可以
                PDDocument document = PDDocument.load(inputStream);
                PDDocumentInformation info = document.getDocumentInformation(); //获得文档属性对象
                info.setTitle(pdfName); //此方法可以修改pdf预览文件名
                document.setDocumentInformation(info);
                document.save(out); //输出
                document.close();
            }
    }

    /**
     * 下载pdf文件
     */
    @GetMapping("/download/{id}")
    public void downloadPDF(@PathVariable Long id, HttpServletResponse response) throws IOException {

        // 获取pdf文件
        PdfFile pdfFile = Db.lambdaQuery(PdfFile.class).eq(id != null, PdfFile::getId, id).one();

        // 二进制文件内容
        byte[] content = pdfFile.getFileContent();

        if (null != content && content.length > 0) {
            String fileName = pdfFile.getFileName();
            // 设置响应内容类型为 PDF
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 将二进制数据写入 HttpServletResponse 的输出流
            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(content);
                outputStream.flush();
            }
        }
    }

}
