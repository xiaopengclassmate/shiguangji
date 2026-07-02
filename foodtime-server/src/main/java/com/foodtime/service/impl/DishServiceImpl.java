package com.foodtime.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.foodtime.constant.MessageConstant;
import com.foodtime.constant.StatusConstant;
import com.foodtime.dto.DishDTO;
import com.foodtime.dto.DishPageQueryDTO;
import com.foodtime.entity.Dish;
import com.foodtime.entity.DishFlavor;
import com.foodtime.entity.Setmeal;
import com.foodtime.exception.DeletionNotAllowedException;
import com.foodtime.mapper.DishFlavorMapper;
import com.foodtime.mapper.DishMapper;
import com.foodtime.mapper.SetmealDishMapper;
import com.foodtime.mapper.SetmealMapper;
import com.foodtime.result.PageResult;
import com.foodtime.service.DishService;
import com.foodtime.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
 private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
//向菜品插入1条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.insert(dish);

        //获取菜品的 id

Long dishId = dish.getId();

        //向口味插入多条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors !=null && flavors.size() > 0){
flavors.forEach(dishFlavor -> {
    dishFlavor.setDishId(dishId);
});
            dishFlavorMapper.insertBatch(flavors);
 }


    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除--是否存在起售中的菜品

      for (Long id : ids){
          Dish dish = dishMapper.getById(id);
          if(dish.getStatus() == StatusConstant.ENABLE){
              throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);

          }
      }

        //判断当前菜品是否能够删除--是否被套餐关联
List<Long> setmealIds= setmealDishMapper.getSetmealIdsByDishId(ids);
      if(setmealIds != null && setmealIds.size() > 0){
          //当前菜品被套餐关联，不能删除
          throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
      }

        //删除菜品表的菜品数据
        for(Long id : ids){
            dishMapper.deleteById(id);

            //删除DishFlavor表的菜品数据
            dishFlavorMapper.deleteByDishId(id);
        }

        //删除DishFlavor表的菜品数据

    }

    @Override
    public DishVO getByIdWithFlavors(Long id) {

        //根据id查询菜品数据

        Dish dish = dishMapper.getById(id);
        //根据菜品id查询到口味数据

        List<DishFlavor> dishFlavors =dishMapper.getByIdWithFlavors(id);
        //将数据封装到VO
        DishVO dishVo = new DishVO();
        BeanUtils.copyProperties(dish,dishVo);
        dishVo.setFlavors(dishFlavors);

        return dishVo;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表基本信息
dishMapper.update(dish);
        //删除原有的口味数据
dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //重新插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //重新插入新的口味数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }
//启售停售
    @Override
    @Transactional
    public void statusOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id( id)
                .status( status)
                .build();
        dishMapper.update(dish);


        if(status == StatusConstant.DISABLE){
            List< Long> dishId = new ArrayList<>();
            dishId.add(id);
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(dishId);
            if(setmealIds != null && setmealIds.size() > 0){
                for (Long setmealId : setmealIds){
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }

    }

    @Override
    public List<Dish> list(Long categoryId) {
         Dish dish= Dish.builder().categoryId(categoryId).status(StatusConstant.ENABLE).build();
         return  dishMapper.list(dish);
    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishMapper.getByIdWithFlavors(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }


}
