package com.foodtime.controller.admin;

import com.foodtime.dto.DishDTO;
import com.foodtime.dto.DishPageQueryDTO;
import com.foodtime.entity.Dish;
import com.foodtime.mapper.DishFlavorMapper;
import com.foodtime.result.PageResult;
import com.foodtime.result.Result;
import com.foodtime.service.DishService;
import com.foodtime.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RequestMapping("/admin/dish")
@RestController
@Api(tags="新增菜品分类")

public class DishController {
    @Autowired
    private  DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping()

    @ApiOperation("新增菜品分类")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品分类", dishDTO);

        dishService.saveWithFlavor(dishDTO);

        //清理缓存数据
        String key="dish_"+dishDTO.getCategoryId();
        redisTemplate.delete(key);
        return Result.success();
    }
    @ApiOperation("查询菜品分类")
    @GetMapping("page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("查询菜品分类", dishPageQueryDTO);
        PageResult pageResult =dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);

    }
    @ApiOperation("删除菜品分类")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除菜品分类", ids);
        dishService.deleteBatch(ids);
clearRedisData("dish_*");
        return Result.success();
    }
    //修改菜品管理

    //根据id查询菜品
    @ApiOperation("根据id找菜品")
    @GetMapping("/{id}")
    public Result<DishVO> update(@PathVariable Long id){
        log.info("修改菜品", id);
        DishVO dishVO = dishService.getByIdWithFlavors(id);
        return Result.success(dishVO);

    }
    @ApiOperation("修改菜品")
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        clearRedisData("dish_*");
        return Result.success();
    }
    @ApiOperation("批量起售停售")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("批量起售停售", status, id);

        dishService.statusOrStop(status, id);
        clearRedisData("dish_*");
        return Result.success();
    }
    @ApiOperation("根据id查询菜品")
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("查询菜品", categoryId);
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }
    /*统一清理数据*/
    private void clearRedisData(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
