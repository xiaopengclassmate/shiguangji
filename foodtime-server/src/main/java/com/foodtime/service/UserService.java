package com.foodtime.service;

import com.foodtime.dto.UserLoginDTO;
import com.foodtime.entity.User;

public interface UserService {
    //微信登录方法、
    User wxLogin(UserLoginDTO userLoginDTO);
}
