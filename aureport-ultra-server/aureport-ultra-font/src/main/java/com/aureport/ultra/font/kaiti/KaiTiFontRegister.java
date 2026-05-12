package com.aureport.ultra.font.kaiti;

import com.aureport.ultra.core.export.pdf.font.FontRegister;

/**
 * @author Jacky.gao
 * @since 2014年5月7日
 */
public class KaiTiFontRegister implements FontRegister {

    public String getFontName() {
        return "楷体";
    }

    public String getFontPath() {
        return "com/aureport/ultra/font/kaiti/SIMKAI.TTF";
    }
}
