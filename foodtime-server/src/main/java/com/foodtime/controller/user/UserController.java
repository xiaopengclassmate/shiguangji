package com.foodtime.controller.user;

import com.foodtime.constant.JwtClaimsConstant;
import com.foodtime.dto.UserLoginDTO;
import com.foodtime.entity.User;
import com.foodtime.properties.JwtProperties;
import com.foodtime.properties.UserJwtProperties;
import com.foodtime.result.Result;
import com.foodtime.service.UserService;
import com.foodtime.utils.JwtUtil;
import com.foodtime.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Api(tags = "C端用户接口")
@Slf4j
@RequestMapping("/user/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserJwtProperties userJwtProperties;
    @PostMapping("/login")
    @ApiOperation("微信登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("微信用户登录");
        User user = userService.wxLogin(userLoginDTO);


        //为微信用户生成jwt令牌
        Map<String, Object> claims =new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());


        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);


                UserLoginVO userLoginVO = UserLoginVO.builder()
                        .id(user.getId())
                        .openid(user.getOpenid())
                        .token(token)
                        .build();

        return Result.success(userLoginVO);
    }
}
