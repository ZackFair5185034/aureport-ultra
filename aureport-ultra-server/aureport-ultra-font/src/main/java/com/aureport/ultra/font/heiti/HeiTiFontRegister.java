package com.aureport.ultra.font.heiti;

import com.aureport.ultra.core.export.pdf.font.FontRegister;

/**
 * @author Jacky.gao
 * @since 2014年5月7日
 */
public class HeiTiFontRegister implements FontRegister {

    public String getFontName() {
        return "黑体";
    }

    public String getFontPath() {
        return "com/aureport/ultra/font/heiti/SIMHEI.TTF";
    }
}
