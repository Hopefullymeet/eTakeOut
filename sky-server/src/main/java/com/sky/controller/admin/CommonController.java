package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.CommonService;
import com.sky.service.impl.CommonServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Api(tags = "普通接口")
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    @Autowired
    CommonService commonService;

    /**
     *上传文件接口
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result<String> upload(MultipartFile file) {
        String fileName = commonService.upload(file);

        return Result.success(fileName);
    }
}
