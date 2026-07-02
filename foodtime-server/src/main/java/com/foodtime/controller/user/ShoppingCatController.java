package com.foodtime.controller.user;

import com.foodtime.dto.ShoppingCartDTO;
import com.foodtime.entity.ShoppingCart;
import com.foodtime.result.Result;
import com.foodtime.service.ShoppingCatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端-购物车接口")
public class ShoppingCatController {
@Autowired
private ShoppingCatService shoppingCatService;
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车：{}", shoppingCartDTO);
shoppingCatService.addshoppingCat(shoppingCartDTO);
return Result.success();

    }


    @GetMapping("list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list(){
        log.info("查看购物车");
        return Result.success(shoppingCatService.list());
    }
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean(){
        log.info("清空购物车");
        shoppingCatService.clean();
        return Result.success();
    }
    @PostMapping("/sub")
    @ApiOperation("删除购物车中的单条数据数据")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车中的单条数据数据");
        shoppingCatService.sub(shoppingCartDTO);
        return Result.success();
    }
}
