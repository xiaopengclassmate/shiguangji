package com.foodtime.service;

import com.foodtime.dto.CategoryDTO;
import com.foodtime.dto.CategoryPageQueryDTO;
import com.foodtime.entity.Category;
import com.foodtime.result.PageResult;
import java.util.List;

public interface CategoryService {


/**
     * 新增分类
     * @param categoryDTO*/
  void save(CategoryDTO categoryDTO);

  PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

  void startOrStop(Integer status, Long id);

  void update(CategoryDTO categoryDTO);

  void deleteById(Long id);

  List<Category> list(Integer type);
}
