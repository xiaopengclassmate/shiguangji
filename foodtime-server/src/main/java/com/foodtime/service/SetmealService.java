package com.foodtime.service;

import com.foodtime.dto.SetmealDTO;
import com.foodtime.dto.SetmealPageQueryDTO;
import com.foodtime.entity.Dish;
import com.foodtime.entity.Setmeal;
import com.foodtime.result.PageResult;
import com.foodtime.vo.DishItemVO;
import com.foodtime.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    void saveWithDish(SetmealDTO setmealDTO);


    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteBatch(List<Long> ids);

    void update(SetmealDTO setmealDTO);

    SetmealVO getByIdWithDish(Long id);

    void startOrStop(Integer status, Long id);
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);

}
