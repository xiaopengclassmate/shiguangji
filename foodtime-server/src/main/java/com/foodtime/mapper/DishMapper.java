package com.foodtime.mapper;

import com.github.pagehelper.Page;
import com.foodtime.annotation.AutoFill;
import com.foodtime.dto.DishPageQueryDTO;
import com.foodtime.entity.Dish;
import com.foodtime.entity.DishFlavor;
import com.foodtime.enumeration.OperationType;
import com.foodtime.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

@AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);


    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);
    //根据id查询菜品和对应的口味

@Select("select * from dish where id = #{id}")
    Dish getById(Long id);
@Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    @Select("select * from dish_flavor where dish_id = #{id}")

    List<DishFlavor> getByIdWithFlavors(Long id);
@AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    List<Dish> list(Dish dish);
    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 更新菜品库存
     * @param id 菜品ID
     * @param stock 新库存数量
     */
    @Update("update dish set stock = #{stock} where id = #{id}")
    void updateStock(Long id, Integer stock);

}
