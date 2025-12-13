package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {
    @Value("${ad-system.upload-path}")
    private String uploadPath;

    @Value("${ad-system.access-prefix}")
    private String accessPrefix; // 对应 "/ad-resource/"

    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        // 1. 生成文件名和日期目录 (保持你之前的逻辑)
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        String datePath = sdf.format(new Date());

        // 2. 确保存储目录存在
        File dir = new File(uploadPath + datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 3. 保存文件到物理磁盘
        // 最终路径例如：D:/data/ad-files/2023/12/13/uuid.mp4
        file.transferTo(new File(dir, fileName));

        // 4. 返回可访问的 Web URL
        // 拼接逻辑：URL前缀 + 日期目录 + 文件名
        // 结果例如：/ad-resource/2023/12/13/uuid.mp4
        // 访问格式：http://localhost:8820/api/ad-resource/2025/12/14/ad75beae-e61a-418b-9b97-8a3d7c170ab4.jpg
        return ResultUtils.success(accessPrefix + datePath + fileName);
    }
}