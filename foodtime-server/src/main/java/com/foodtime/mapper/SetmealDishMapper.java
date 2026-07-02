package com.foodtime.mapper;

import com.foodtime.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /*
    *
    * 根据菜品id查询套餐id*/
    //select setmeal_id from setmeal_dish where dish_id in (?,?,?)
    List<Long> getSetmealIdsByDishId(List<Long> dishId);

    void insertBatch(List<SetmealDish> setmealDishes);
@Delete("delete from setmeal_dish where setmeal_id = #{id}")
    void deleteBySetmealId(Long id);

    List<SetmealDish> getSetmealId(Long id);
}
