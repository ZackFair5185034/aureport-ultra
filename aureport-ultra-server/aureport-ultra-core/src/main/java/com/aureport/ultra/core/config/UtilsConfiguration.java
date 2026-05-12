package com.aureport.ultra.core.config;

import com.aureport.ultra.core.UReportPropertyPlaceholderConfigurer;
import com.aureport.ultra.core.Utils;
import com.aureport.ultra.core.cache.CacheUtils;
import com.aureport.ultra.core.expression.ExpressionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilsConfiguration {

    @Bean
    public UReportPropertyPlaceholderConfigurer uReportPropertyPlaceholderConfigurer() {
        UReportPropertyPlaceholderConfigurer configurer = new UReportPropertyPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }

    @Bean
    public ExpressionUtils expressionUtils() {
        return new ExpressionUtils();
    }

    @Bean
    public Utils utils(@Value("${aureport-ultra.debug:false}") boolean debug) {
        Utils utils = new Utils();
        utils.setDebug(debug);
        return utils;
    }

    @Bean
    public CacheUtils cacheUtils() {
        return new CacheUtils();
    }
}
