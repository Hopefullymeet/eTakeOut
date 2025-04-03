package com.sky.service.impl;

import com.sky.service.CommonService;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Autowired
    AliOssUtil aliOssUtil;

    /**
     * 上传文件
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();

            String suffix = originalName.substring(originalName.lastIndexOf('.'));

            log.info("文件后缀名为{}", suffix);

            return aliOssUtil.upload(file.getBytes(), UUID.randomUUID().toString() + suffix);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
