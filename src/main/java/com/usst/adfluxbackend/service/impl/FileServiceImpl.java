package com.usst.adfluxbackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.usst.adfluxbackend.exception.BusinessException;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.model.enums.MediaType;
import com.usst.adfluxbackend.model.vo.FileVO;
import com.usst.adfluxbackend.service.IFileService;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class FileServiceImpl implements IFileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            // 图片
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
            // 视频
            ".mp4", ".mov", ".avi", ".mkv", ".flv", ".webm"
    );

    @Value("${ad-system.upload-path}")
    private String uploadPath;

    @Value("${ad-system.access-prefix}")
    private String accessPrefix;

    @Value("${media.ffmpeg.ffprobe-path}")
    private String ffprobePath;

    @Override
    public FileVO uploadMediaFile(MultipartFile file) {
        File tempFile = null;
        try {
            // 1. 保存为临时文件
            String originalFilename = file.getOriginalFilename();
            String suffix = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";

            tempFile = File.createTempFile("upload_tmp_", suffix);
            file.transferTo(tempFile);

            String fileName = tempFile.getName();
            fileName = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
            // 判断后缀是否在白名单中
            if (!ALLOWED_EXTENSIONS.contains(fileName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的媒体格式，仅支持 JPG, PNG, MP4 等常见类型");
            }

            // 2. FFprobe 解析与校验
            MediaType mediaType = analyzeMediaType(tempFile);

            // 如果是非法文件，直接抛异常
            if (mediaType == MediaType.ILLEGAL) {
                throw new BusinessException(1, "文件校验失败：格式非法或文件已损坏");
            }

            // 3. 计算 Hash (去重/秒传)
            String fileHash = SecureUtil.sha256(tempFile);

            // 4. 正式存储逻辑
            String finalFileName = fileHash + suffix;

            File destDir = new File(uploadPath);
            if (!destDir.exists()) destDir.mkdirs();
            File finalDestFile = new File(destDir, finalFileName);

            // 如果文件不存在，则移动；如果存在则视为秒传（无需移动，直接使用旧文件）
            if (!finalDestFile.exists()) {
                FileUtil.move(tempFile, finalDestFile, true);
                log.info("文件上传成功: {}", finalFileName);
            } else {
                log.info("文件秒传命中: {}", finalFileName);
                FileUtil.del(tempFile); // 删除临时文件
            }

            // 5. 封装返回对象 VO
            String fullUrl = accessPrefix + finalFileName;

            FileVO fileVO = new FileVO();
            fileVO.setMediaUrl(fullUrl);
            fileVO.setAdType(mediaType.getValue()); // 获取枚举中的 0 或 1

            return fileVO;

        } catch (Exception e) {
            log.error("上传处理异常", e);
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * FFprobe 分析逻辑
     */
    private MediaType analyzeMediaType(File file) {
        try {
            FFprobe ffprobe = new FFprobe(ffprobePath);
            FFmpegProbeResult result = ffprobe.probe(file.getAbsolutePath());

            if (result == null || result.getFormat() == null) return MediaType.ILLEGAL;

            String formatName = result.getFormat().format_name;
            log.info("FFprobe 检测格式: {}", formatName);

            // 视频白名单
            if (formatName.contains("mp4") || formatName.contains("mov")
                    || formatName.contains("avi") || formatName.contains("matroska")
                    || formatName.contains("flv")) {
                return MediaType.VIDEO;
            }

            // 图片白名单
            if (formatName.contains("image2") || formatName.contains("png")
                    || formatName.contains("jpeg") || formatName.contains("gif")
                    || formatName.contains("bmp")) {
                return MediaType.IMAGE;
            }

            return MediaType.ILLEGAL;

        } catch (IOException e) {
            log.error("FFprobe 解析失败: {}", e.getMessage());
            return MediaType.ILLEGAL;
        }
    }
}