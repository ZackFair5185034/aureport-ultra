package com.aureport.ultra.font.songti;

import com.aureport.ultra.core.export.pdf.font.FontRegister;

/**
 * @author Jacky.gao
 * @since 2014年5月7日
 */
public class SongTiFontRegister implements FontRegister {

    public String getFontName() {
        return "宋体";
    }

    public String getFontPath() {
        return "com/luck/report/font/songti/SIMSUN.TTC";
    }
}
