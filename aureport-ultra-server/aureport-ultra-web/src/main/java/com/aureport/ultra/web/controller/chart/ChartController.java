package com.aureport.ultra.web.controller.chart;

import com.aureport.ultra.core.cache.CacheUtils;
import com.aureport.ultra.core.chart.ChartData;
import com.aureport.ultra.core.utils.UnitUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;


/**
 * 图表控制器
 * 替代原有的ChartServletAction，提供图表数据存储功能
 */
@RestController("bean.chartController")
@RequestMapping(value = "${aureport-ultra.servletPrefix}/chart", method = RequestMethod.GET)
@Tag(name = "图表", description = "图表数据存储与管理")
public class ChartController {

    /**
     * 存储图表数据
     */
    @Operation(
        summary = "存储图表数据",
        parameters = {
            @Parameter(name = "_chartId", description = "图表ID", required = true, example = "chart_001"),
            @Parameter(name = "_base64Data", description = "图表图片Base64数据", required = false, example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="),
            @Parameter(name = "_width", description = "图表宽度(像素)", required = false, example = "800"),
            @Parameter(name = "_height", description = "图表高度(像素)", required = false, example = "600")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "存储成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/storeData", method = RequestMethod.GET)
    public void storeData(HttpServletRequest req) {
        String chartId = req.getParameter("_chartId");
        ChartData chartData = CacheUtils.getChartData(chartId);
        if (chartData == null) {
            return;
        }
        String base64Data = req.getParameter("_base64Data");
        String prefix = "data:image/png;base64,";
        if (base64Data != null) {
            if (base64Data.startsWith(prefix)) {
                base64Data = base64Data.substring(prefix.length());
            }
        }
        chartData.setBase64Data(base64Data);
        String width = req.getParameter("_width");
        String height = req.getParameter("_height");
        chartData.setHeight(UnitUtils.pixelToPoint(Integer.valueOf(height)));
        chartData.setWidth(UnitUtils.pixelToPoint(Integer.valueOf(width)));
    }

    // 辅助方法：解码
    protected String decode(String value) {
        if (value == null) {
            return value;
        }
        try {
            value = URLDecoder.decode(value, "utf-8");
            value = URLDecoder.decode(value, "utf-8");
            return value;
        } catch (Exception ex) {
            return value;
        }
    }


}
