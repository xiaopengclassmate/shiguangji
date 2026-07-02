package com.foodtime.controller.admin;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.foodtime.dto.SetmealDTO;
import com.foodtime.dto.SetmealPageQueryDTO;
import com.foodtime.entity.Dish;
import com.foodtime.result.PageResult;
import com.foodtime.result.Result;
import com.foodtime.service.SetmealService;
import com.foodtime.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/setmeal")
@Api(tags="套餐管理")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache", key="#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();

    }
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询", setmealPageQueryDTO);
        PageResult pageResult =setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
@DeleteMapping
@ApiOperation("批量删除套餐")
@CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
}
@ApiOperation("根据id查询套餐和关联数据和菜品数据")
@GetMapping("/{id}")
public Result<SetmealVO> getById(@PathVariable Long id){
    log.info("根据id查询套餐和关联数据", id);
    SetmealVO setmealVO = setmealService.getByIdWithDish(id);
    return Result.success(setmealVO);
}
@ApiOperation("修改套餐")
@PutMapping
@CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
}
@ApiOperation("起售停售套餐")
@PostMapping("/status/{status}")
@CacheEvict(cacheNames = "setmealCache", allEntries = true)
public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("起售停售套餐", status, id);
        setmealService.startOrStop(status, id);
        return Result.success();

}

}
