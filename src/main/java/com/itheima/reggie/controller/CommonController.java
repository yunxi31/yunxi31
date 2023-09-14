package com.itheima.reggie.controller;


import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
@Value("${reggie.path}")
    private String basePath;

 /*
 * 文件上传
 * */
@PostMapping("/upload")
    public R<String> upload(MultipartFile file){       //这里要和前端保持一致，不能乱写Mutipartfile对象，upload界面发送的请求中，from data中name属性保持一致
      //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会被删除
       log.info(file.toString());
    //原始文件名
    String originalFilename = file.getOriginalFilename();//abc.jpg
    String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

    //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
    String fileName = UUID.randomUUID().toString() + suffix;       //dfsdfdfd.jpg

    //创建一个目录对象
    File dir = new File(basePath);
    //判断当前目录是否存在
    if(!dir.exists()){
        //目录不存在，需要创建
        dir.mkdirs();
    }
    try {
        //将临时文件转存到指定位置
        file.transferTo(new File(basePath+fileName));
    } catch (IOException e) {
        e.printStackTrace();
    }
    return R.success(fileName);
    }



    /*
    * 文件下载            这个我也有问题
    * */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        //输入流，通过输入流读取文件
        try {
            FileInputStream fileInputStream=new FileInputStream(new File(basePath+name));

            //输出流,将文件写回浏览器
            ServletOutputStream outputStream=response.getOutputStream();
            response.setContentType("image/jpeg");

            int len=0;
            byte[] byetes=new byte[1024];
            while ((len=fileInputStream.read(byetes))!=-1){
                outputStream.write(byetes,0,len);
                outputStream.flush();


                //关闭资源
                outputStream.close();
                fileInputStream.close();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
