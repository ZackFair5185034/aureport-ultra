package com.aureport.ultra.web.controller.excel;

import com.aureport.ultra.core.build.ReportBuilder;
import com.aureport.ultra.core.definition.ReportDefinition;
import com.aureport.ultra.core.export.ExportConfigure;
import com.aureport.ultra.core.export.ExportConfigureImpl;
import com.aureport.ultra.core.export.ExportManager;
import com.aureport.ultra.core.export.excel.low.Excel97Producer;
import com.aureport.ultra.core.model.Report;
import com.aureport.ultra.web.cache.TempObjectCache;
import com.aureport.ultra.web.constant.ReportConstants;
import com.aureport.ultra.web.exception.ReportDesignException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Excel 97-2003导出控制器
 * 替代原有的ExportExcel97ServletAction，提供Excel 97-2003格式导出功能
 */
@RestController("bean.exportExcel97Controller")
@RequestMapping(value = "${aureport-ultra.servletPrefix}/excel97", method = RequestMethod.GET)
@Tag(name = "Excel 97-2003导出", description = "Excel 97-2003格式(.xls)报表导出功能")
public class ExportExcel97Controller {

    @Autowired
    private ReportBuilder reportBuilder;

    @Autowired
    private ExportManager exportManager;

    private final Excel97Producer excelProducer = new Excel97Producer();

    @Operation(
        summary = "构建Excel 97报表",
        parameters = {
            @Parameter(name = "reportPath", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml"),
            @Parameter(name = "_n", description = "导出文件名(不含扩展名)", required = false, example = "report"),
            @Parameter(name = "mode", description = "预览模式(preview为预览)", required = false, example = "preview")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Excel 97文件流(.xls)", content = @Content(mediaType = "application/vnd.ms-excel")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/build", method = RequestMethod.GET)
    public void build(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        buildExcel(req, resp, false, false);
    }

    @Operation(
        summary = "分页导出Excel 97报表",
        parameters = {
            @Parameter(name = "reportPath", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml"),
            @Parameter(name = "_n", description = "导出文件名(不含扩展名)", required = false, example = "report"),
            @Parameter(name = "mode", description = "预览模式(preview为预览)", required = false, example = "preview")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Excel 97文件流(.xls)", content = @Content(mediaType = "application/vnd.ms-excel")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public void paging(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        buildExcel(req, resp, true, false);
    }

    @Operation(
        summary = "按Sheet导出Excel 97报表",
        parameters = {
            @Parameter(name = "reportPath", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml"),
            @Parameter(name = "_n", description = "导出文件名(不含扩展名)", required = false, example = "report"),
            @Parameter(name = "mode", description = "预览模式(preview为预览)", required = false, example = "preview")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Excel 97文件流(.xls)", content = @Content(mediaType = "application/vnd.ms-excel")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/sheet", method = RequestMethod.GET)
    public void sheet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        buildExcel(req, resp, false, true);
    }

    private void buildExcel(HttpServletRequest req, HttpServletResponse resp, boolean withPage, boolean withSheet) throws IOException {
        String fileName = req.getParameter("reportPath");
        fileName = decode(fileName);
        String mode = req.getParameter("mode");
        boolean isPreview = ReportConstants.MODE_KEY.equals(mode);
        String excelName = req.getParameter("_n");
        if (StringUtils.isNotBlank(excelName)) {
            excelName = decode(excelName);
        } else {
            excelName = "aureport-ultra.xls";
        }
        resp.setContentType("application/octet-stream;charset=ISO8859-1");
        resp.setHeader("Content-Disposition", "attachment;filename=\"" + excelName + "\"");
        Map<String, Object> parameters = buildParameters(req);
        OutputStream outputStream = resp.getOutputStream();
        if (isPreview) {
            ReportDefinition reportDefinition = (ReportDefinition) TempObjectCache.getObject(fileName);
            if (reportDefinition == null) {
                throw new ReportDesignException("Report data has expired,can not do export excel.");
            }
            Report report = reportBuilder.buildReport(reportDefinition, parameters);
            if (withPage) {
                excelProducer.produceWithPaging(report, outputStream);
            } else if (withSheet) {
                excelProducer.produceWithSheet(report, outputStream);
            } else {
                excelProducer.produce(report, outputStream);
            }
        } else {
            ExportConfigure configure = new ExportConfigureImpl(fileName, parameters, outputStream);
            if (withPage) {
                exportManager.exportExcelWithPaging(configure);
            } else if (withSheet) {
                exportManager.exportExcelWithPagingSheet(configure);
            } else {
                exportManager.exportExcel(configure);
            }
        }
        outputStream.flush();
        outputStream.close();
    }

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

    protected Map<String, Object> buildParameters(HttpServletRequest req) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        Enumeration<?> enumeration = req.getParameterNames();
        while (enumeration.hasMoreElements()) {
            Object obj = enumeration.nextElement();
            if (obj == null) {
                continue;
            }
            String name = obj.toString();
            String value = req.getParameter(name);
            if (name == null || value == null || name.startsWith("_")) {
                continue;
            }
            parameters.put(name, decode(value));
        }
        return parameters;
    }
}
