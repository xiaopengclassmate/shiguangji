package com.foodtime.service;

import com.foodtime.dto.ShoppingCartDTO;
import com.foodtime.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCatService {
    void addshoppingCat(ShoppingCartDTO shoppingCartDTO);


    List<ShoppingCart> list();

    void clean();

    void sub(ShoppingCartDTO shoppingCartDTO);
}
