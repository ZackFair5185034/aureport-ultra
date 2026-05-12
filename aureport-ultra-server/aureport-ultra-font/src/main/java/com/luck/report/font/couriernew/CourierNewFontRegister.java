package com.aureport.ultra.font.couriernew;

import com.aureport.ultra.core.export.pdf.font.FontRegister;

/**
 * @author Jacky.gao
 * @since 2014年5月7日
 */
public class CourierNewFontRegister implements FontRegister {

    public String getFontName() {
        return "Courier New";
    }

    public String getFontPath() {
        return "com/luck/report/font/couriernew/COUR.TTF";
    }
}
