package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        //构造redis中的key，规则：dish_分类id
        String key = "dish_" + categoryId;

        //查询redis中是否存在
        String jsonFromRedis = stringRedisTemplate.opsForValue().get(key);
        if(jsonFromRedis!=null){
            log.info("内存");
            List<DishVO> dishVOListFromRedis = JSON.parseObject(jsonFromRedis, new TypeReference<List<DishVO>>() {});
            return Result.success(dishVOListFromRedis);
        }
        else {
            log.info("磁盘");
            Dish dish = new Dish();
            dish.setCategoryId(categoryId);
            dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
            List<DishVO> list = dishService.listWithFlavor(dish);
            if(!list.isEmpty()){
                String dishList  = JSON.toJSONString(list);
                stringRedisTemplate.opsForValue().set(key,dishList);
            }

            return Result.success(list);
        }





    }

}
