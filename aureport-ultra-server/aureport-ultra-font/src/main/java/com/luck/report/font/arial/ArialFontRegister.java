package com.aureport.ultra.font.arial;

import com.aureport.ultra.core.export.pdf.font.FontRegister;


/**
 * @author Jacky.gao
 * @since 2014年5月7日
 */
public class ArialFontRegister implements FontRegister {

    public String getFontName() {
        return "Arial";
    }

    public String getFontPath() {
        return "com/luck/report/font/arial/ARIAL.TTF";
    }
}
