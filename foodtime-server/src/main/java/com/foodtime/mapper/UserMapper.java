package com.foodtime.mapper;

import com.foodtime.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);

    void insert(User byOpenid);
    @Select("select * from user where id=#{id}")
    User getById(Long id);


    Integer countByMap(Map map);
}
