package com.usst.adfluxbackend.service;

import com.usst.adfluxbackend.model.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    /**
     * 上传并校验媒体文件
     * @param file 前端上传的文件
     * @return 文件信息
     */
    FileVO uploadMediaFile(MultipartFile file);
}