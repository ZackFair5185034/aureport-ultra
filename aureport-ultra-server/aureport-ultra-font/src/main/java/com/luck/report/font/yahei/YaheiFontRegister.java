package com.aureport.ultra.font.yahei;

import com.aureport.ultra.core.export.pdf.font.FontRegister;

/**
 * @author Jacky.gao
 * @since 2014年5月7日
 */
public class YaheiFontRegister implements FontRegister {

    public String getFontName() {
        return "微软雅黑";
    }

    public String getFontPath() {
        return "com/luck/report/font/yahei/msyh.ttc";
    }
}
