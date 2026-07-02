package com.foodtime.interceptor;

import com.foodtime.constant.JwtClaimsConstant;
import com.foodtime.context.BaseContext;
import com.foodtime.properties.JwtProperties;
import com.foodtime.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt token interceptor for admin
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * verify jwt
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // skip non-controller requests
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // get token from request header
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        // verify token
        try {
            log.debug("jwt admin token verify");
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            log.info("current admin empId:{}", empId);
            BaseContext.setCurrentId(empId);
            return true;
        } catch (Exception ex) {
            log.warn("admin jwt verify failed");
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}
