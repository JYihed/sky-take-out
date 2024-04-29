package com.sky.controller.admin;

import com.sky.constant.RedisConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@Api(tags = "店铺相关接口")
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置店铺状态
     * @param status
     * @return
     */
    @ApiOperation("设置店铺营业状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        stringRedisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS_KEY,status.toString());
        return Result.success();
    }


    /**
     * 管理端查询店铺状态
     * @return
     */
    @ApiOperation("查询店铺营业状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        String s = stringRedisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS_KEY);
        return Result.success(Integer.parseInt(s));
    }
}
