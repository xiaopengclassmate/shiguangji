package com.foodtime.controller.admin;

import com.foodtime.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    @ApiOperation("设置店铺营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺营业状态为 ：{}", status ==1 ?"营业中" : "打样中");
        redisTemplate.opsForValue().set("SHOP_STATUS",status);

        return Result.success(status);

    }
    @ApiOperation("查询店铺营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        // 如果 Redis 中没有设置过店铺状态，默认返回打烊中(0)
        if (status == null) {
            status = 0;
        }
        log.info("查询营业状态为 {}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
