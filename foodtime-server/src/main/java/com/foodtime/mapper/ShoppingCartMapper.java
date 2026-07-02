package com.foodtime.mapper;

import com.foodtime.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    //根据id查询商品数量
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void update(ShoppingCart shoppingCart);
@Insert("insert into shopping_cart (name,user_id, image, dish_id, setmeal_id, dish_flavor, number, amount, create_time) values (#{name},#{userId}, #{image}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);
@Delete("delete from shopping_cart where user_id=#{userId}")
    void deleteByUserId(Long currentId);
//根据id删除购物车数据
@Delete("delete from shopping_cart where id =#{id}")

    void sub(Long  id);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
