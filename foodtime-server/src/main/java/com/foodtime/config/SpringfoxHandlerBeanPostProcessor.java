package com.foodtime.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 修复 Springfox 3.0.0 在 Spring Boot 2.6+ 上的 NPE 兼容性问题
 *
 * 原因：Spring Boot 2.6+ 默认使用 PathPatternParser，
 * 而 Springfox 3.0.0 内部依赖 AntPathMatcher 的 PatternsRequestCondition，
 * 当 condition 为 null 时调用 getPatterns() 抛出 NPE。
 *
 * 此 Bean 通过反射替换 WebMvcRequestHandlerProvider 中的 handlerMappings，
 * 过滤掉导致 NPE 的 actuator endpoint 映射。
 */
@Configuration
public class SpringfoxHandlerBeanPostProcessor {

    @Bean
    public BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    fixSpringfoxHandlerMappings(bean);
                }
                return bean;
            }

            private void fixSpringfoxHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    if (field != null) {
                        field.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        List<RequestMappingInfoHandlerMapping> mappings =
                                (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                        List<RequestMappingInfoHandlerMapping> filtered = mappings.stream()
                                .filter(mapping -> mapping.getPatternParser() == null)
                                .collect(Collectors.toList());
                        field.set(bean, filtered);
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        };
    }
}
