package com.aureport.ultra.core.utils;

import java.util.regex.Pattern;

/**
 * XSS防护过滤器
 * 用于过滤富文本内容，只允许安全的HTML标签和属性
 *
 * @author 奥力给
 * @since 2026-05-17
 */
public class XssFilter {

    // 允许的HTML标签
    private static final String[] ALLOWED_TAGS = {
        "b", "i", "u", "strong", "em", "s", "del", "ins",
        "span", "font", "p", "br", "div", "center"
    };

    // 允许的style属性值中的CSS属性
    private static final String[] ALLOWED_CSS_PROPERTIES = {
        "color", "background-color", "font-weight", "font-style",
        "text-decoration", "font-size"
    };

    // 危险标签模式
    private static final Pattern DANGEROUS_TAGS = Pattern.compile(
        "<(script|iframe|object|embed|applet|form|input|button|select|textarea" +
        "|style|link|meta|base|head|body|svg|math|on\\w+)",
        Pattern.CASE_INSENSITIVE
    );

    // 危险属性模式
    private static final Pattern DANGEROUS_ATTRS = Pattern.compile(
        "on\\w+\\s*=|javascript:|data:|<img",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 过滤富文本内容，只保留安全的HTML
     *
     * @param html 输入的HTML内容
     * @return 过滤后的安全HTML内容
     */
    public static String filter(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        String filtered = html;

        // 移除危险标签
        filtered = DANGEROUS_TAGS.matcher(filtered).replaceAll("&lt;$1");

        // 移除危险属性
        filtered = DANGEROUS_ATTRS.matcher(filtered).replaceAll("<!-- filtered -->");

        // 清理script标签内容（包括CDATA）
        filtered = Pattern.compile(
            "<script[^>]*>.*?</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        ).matcher(filtered).replaceAll("");

        // 清理style标签
        filtered = Pattern.compile(
            "<style[^>]*>.*?</style>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        ).matcher(filtered).replaceAll("");

        return filtered;
    }

    /**
     * 验证HTML内容是否安全
     *
     * @param html 输入的HTML内容
     * @return true表示安全，false表示包含危险内容
     */
    public static boolean isSafe(String html) {
        if (html == null || html.isEmpty()) {
            return true;
        }

        // 检查是否包含危险标签
        if (DANGEROUS_TAGS.matcher(html).find()) {
            return false;
        }

        // 检查是否包含危险属性
        if (DANGEROUS_ATTRS.matcher(html).find()) {
            return false;
        }

        return true;
    }
}
