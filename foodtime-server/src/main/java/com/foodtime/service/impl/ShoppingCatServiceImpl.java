package com.foodtime.service.impl;

import com.foodtime.context.BaseContext;
import com.foodtime.dto.ShoppingCartDTO;
import com.foodtime.entity.Dish;
import com.foodtime.entity.Setmeal;
import com.foodtime.entity.ShoppingCart;
import com.foodtime.mapper.DishMapper;
import com.foodtime.mapper.SetmealMapper;
import com.foodtime.mapper.ShoppingCartMapper;
import com.foodtime.service.ShoppingCatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCatServiceImpl implements ShoppingCatService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    //添加购物车
    @Override
    public void addshoppingCat(ShoppingCartDTO shoppingCartDTO) {
//判断当前加入到购物车中的商品是否存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);


        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //如果已经存在，则更新数量+1
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);//update shopping_cart set number = number + 1 where dish_id = ? and setmeal_id = ?
            shoppingCartMapper.update(cart);
        }
        //如果不存在，则添加
        else {
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                //本次添加购物车的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            } else {
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            shoppingCartMapper.insert(shoppingCart);
        }
    }
//查看购物车
    @Override
    public List<ShoppingCart> list() {
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart= ShoppingCart.builder()
                .userId(currentId)

                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    @Override
    public void clean() {
        Long currentId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(currentId);

    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
   ShoppingCart shoppingCart=new ShoppingCart();
   BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
   //设置条件，查询当前登录用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list !=null && list.size()>0){
            ShoppingCart cart = list.get(0);
            if(cart.getNumber()==1){
                shoppingCartMapper.sub( cart.getId());

            }
            else {
                cart.setNumber(cart.getNumber()-1);
                shoppingCartMapper.update(cart);
            }
        }
    }
}