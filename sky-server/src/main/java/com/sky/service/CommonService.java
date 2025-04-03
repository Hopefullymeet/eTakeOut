package com.sky.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
public interface CommonService {
    /**
     * 上传文件
     * @param file
     * @return
     */
    String upload(MultipartFile file);
}
