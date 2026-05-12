package com.aureport.ultra.font.config;

import com.aureport.ultra.font.arial.ArialFontRegister;
import com.aureport.ultra.font.comicsansms.ComicSansMSFontRegister;
import com.aureport.ultra.font.couriernew.CourierNewFontRegister;
import com.aureport.ultra.font.timesnewroman.TimesNewRomanFontRegister;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 英文字体配置类
 */
@Configuration
public class EnglishFontConfig {

    @Bean("bean.arialFontRegister")
    public ArialFontRegister arialFontRegister() {
        return new ArialFontRegister();
    }

    @Bean("bean.comicSansMSFontRegister")
    public ComicSansMSFontRegister comicSansMSFontRegister() {
        return new ComicSansMSFontRegister();
    }

    @Bean("bean.courierNewFontRegister")
    public CourierNewFontRegister courierNewFontRegister() {
        return new CourierNewFontRegister();
    }

    @Bean("bean.timesNewRomanFontRegister")
    public TimesNewRomanFontRegister timesNewRomanFontRegister() {
        return new TimesNewRomanFontRegister();
    }
}
