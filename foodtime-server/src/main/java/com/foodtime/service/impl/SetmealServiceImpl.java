package com.foodtime.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.foodtime.constant.MessageConstant;
import com.foodtime.dto.SetmealDTO;
import com.foodtime.dto.SetmealPageQueryDTO;
import com.foodtime.entity.Dish;
import com.foodtime.entity.Setmeal;
import com.foodtime.entity.SetmealDish;
import com.foodtime.exception.DeletionNotAllowedException;
import com.foodtime.mapper.SetmealDishMapper;
import com.foodtime.mapper.SetmealMapper;
import com.foodtime.result.PageResult;
import com.foodtime.service.SetmealService;
import com.foodtime.vo.DishItemVO;
import com.foodtime.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

public class SetmealServiceImpl implements SetmealService {
@Autowired
private SetmealMapper setmealMapper;

@Autowired

private SetmealDishMapper setmealDishMapper;    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);

        //拿到新增的套餐的id
        Long setmealId = setmeal.getId();


        //向菜品插入多条数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && setmealDishes.size() > 0){
            if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            //批量插入
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }
}

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());

    }
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == 1){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        ids.forEach(id -> {
            setmealMapper.deleteById(id);
            setmealDishMapper.deleteBySetmealId(id);
        });

    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        //拿到的套餐的id
Long setmealId = setmeal.getId();


//删除原有的菜品和套餐数据
        setmealDishMapper.deleteBySetmealId(setmealId);
        List<SetmealDish> setmealDishes= setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

//重新插入套餐和餐品的关联关系，操作setmeal_dish表
        setmealDishMapper.insertBatch(setmealDishes);

    }

    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal byId = setmealMapper.getById(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealId(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(byId,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public void startOrStop(Integer status, Long id) {
       Setmeal setmeal = Setmeal.builder().
               status(status).
               id(id).
               build();
       setmealMapper.update(setmeal);
    }
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

}
