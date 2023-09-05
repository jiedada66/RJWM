package com.itheima.reggie.controller;

import com.itheima.reggie.result.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String path;

    /**
     * 上传图片,参数必须为file，请求方式必须为post
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //file是一个临时文件，需要将其保存到服务器的某个位置，否则上传成功后，临时文件会被删除

        //获取原文件名
        String originalFilename = file.getOriginalFilename();

        //获取文件后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用uuid重新生成文件名，防止文件名重复造成覆盖
        String fileName = UUID.randomUUID() + suffix;

        //创建一个目录对象
        File dir = new File(path);
        //如果目录不存在，则创建目录
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(path + fileName)); //保存文件
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);
    }

    /**
     * 下载图片
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response)  throws Exception{
        //输入流读取文件
        FileInputStream fis = new FileInputStream(path + name);

        //通过输出流将文件写回浏览器，展示给用户
        response.setContentType("image/jpeg"); //设置响应的内容类型为图片

        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) != -1) {
            response.getOutputStream().write(buffer, 0, len);
        }
        fis.close();

    }

}
