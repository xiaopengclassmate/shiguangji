package com.foodtime.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.foodtime.constant.MessageConstant;
import com.foodtime.dto.UserLoginDTO;
import com.foodtime.entity.User;
import com.foodtime.exception.LoginFailedException;
import com.foodtime.mapper.UserMapper;
import com.foodtime.properties.WeChatProperties;
import com.foodtime.service.UserService;
import com.foodtime.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    static  final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties wx;
    @Autowired
    private UserMapper userMapper;
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());


        //判断openid是否为空，为空则表示登录失败
if(openid == null){
    throw  new LoginFailedException(MessageConstant.LOGIN_FAILED);

}
        //判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);

        //如果是新用户，自动完成注册
if(user == null){
    user=User.builder().openid(openid).createTime(LocalDateTime.now()).build();
    userMapper.insert(user);
}

        //返回这个用户对象

return user;
    }
//调用微信接口服务，获得当前微信用户的openid
    private String getOpenid(String code) {
        //调用微信接口服务，获得当前微信用户的openid
        Map<String, String> map = new HashMap<>();
        map.put("appid",wx.getAppid());
        map.put("secret",wx.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");

        return openid;
    }



}
