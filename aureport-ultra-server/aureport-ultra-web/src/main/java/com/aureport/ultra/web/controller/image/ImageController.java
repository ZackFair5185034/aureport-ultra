package com.aureport.ultra.web.controller.image;

import com.aureport.ultra.core.cache.ResourceCache;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 图片控制器
 * 替代原有的ImageServletAction，提供图片资源访问功能
 */
@RestController("bean.imageController")
@RequestMapping(value = "${aureport-ultra.servletPrefix}/image", method = RequestMethod.GET)
@Tag(name = "图片", description = "图片资源获取与管理")
public class ImageController {

    /**
     * 获取图片资源
     */
    @Operation(
        summary = "获取图片资源",
        parameters = {
            @Parameter(name = "_key", description = "图片缓存Key", required = true, example = "chart_001_image")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "PNG图片流", content = @Content(mediaType = "image/png")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public void getImage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String key = req.getParameter("_key");
        if (StringUtils.isNotBlank(key)) {
            byte[] bytes = (byte[]) ResourceCache.getObject(key);
            InputStream input = new ByteArrayInputStream(bytes);
            OutputStream output = resp.getOutputStream();
            resp.setContentType("image/png");
            try {
                IOUtils.copy(input, output);
            } finally {
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(output);
            }
        }
    }
}
