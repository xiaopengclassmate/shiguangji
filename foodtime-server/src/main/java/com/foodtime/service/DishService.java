package com.foodtime.service;

import com.foodtime.dto.DishDTO;
import com.foodtime.dto.DishPageQueryDTO;
import com.foodtime.entity.Dish;
import com.foodtime.result.PageResult;
import com.foodtime.vo.DishVO;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

public interface DishService {
    /*新增菜品和口味*/
    void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    DishVO getByIdWithFlavors(Long id);

    //根据id修改菜品信息

    void updateWithFlavor(DishDTO dishDTO);

    void statusOrStop(Integer status, Long id);

    List<Dish> list(Long categoryId);
    List<DishVO> listWithFlavor(Dish dish);
}
