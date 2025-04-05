package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺营业状态接口")
@Slf4j
public class ShopController {

    @Autowired
    ShopService shopService;

    /**
     * 设置店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setShopStatus(@PathVariable Integer status) {

        Integer result = shopService.setShopStatus(status);

        return Result.success(result);
    }

    /**
     * 获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result getShopStatus() {
        Integer result = shopService.getShopStatus();

        return Result.success(result);
    }

}
