package com.aureport.ultra.web.controller.excel;

import com.aureport.ultra.core.build.ReportBuilder;
import com.aureport.ultra.core.definition.ReportDefinition;
import com.aureport.ultra.core.exception.ReportComputeException;
import com.aureport.ultra.core.exception.ReportException;
import com.aureport.ultra.core.export.ExportConfigure;
import com.aureport.ultra.core.export.ExportConfigureImpl;
import com.aureport.ultra.core.export.ExportManager;
import com.aureport.ultra.core.export.excel.high.ExcelProducer;
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
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Excel导出控制器
 * 替代原来的ExportExcelServletAction
 */
@RestController("bean.exportExcelController")
@RequestMapping(value = "${aureport-ultra.servletPrefix}/excel", method = RequestMethod.GET)
@Tag(name = "Excel导出", description = "Excel 2007+格式(.xlsx)报表导出功能")
public class ExportExcelController {

    @Autowired
    private ReportBuilder reportBuilder;

    @Autowired
    private ExportManager exportManager;

    private final ExcelProducer excelProducer = new ExcelProducer();

    /**
     * 构建Excel报表
     */
    @Operation(
        summary = "构建Excel报表",
        parameters = {
            @Parameter(name = "reportPath", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml"),
            @Parameter(name = "_n", description = "导出文件名(不含扩展名)", required = false, example = "report"),
            @Parameter(name = "mode", description = "预览模式(preview为预览)", required = false, example = "preview")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Excel文件流(.xlsx)", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/build", method = RequestMethod.GET)
    public void build(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        buildExcel(req, resp, false, false);
    }

    /**
     * 分页导出Excel报表
     */
    @Operation(
        summary = "分页导出Excel报表",
        parameters = {
            @Parameter(name = "reportPath", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml"),
            @Parameter(name = "_n", description = "导出文件名(不含扩展名)", required = false, example = "report"),
            @Parameter(name = "mode", description = "预览模式(preview为预览)", required = false, example = "preview")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Excel文件流(.xlsx)", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public void paging(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        buildExcel(req, resp, true, false);
    }

    /**
     * 按Sheet导出Excel报表
     */
    @Operation(
        summary = "按Sheet导出Excel报表",
        parameters = {
            @Parameter(name = "reportPath", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml"),
            @Parameter(name = "_n", description = "导出文件名(不含扩展名)", required = false, example = "report"),
            @Parameter(name = "mode", description = "预览模式(preview为预览)", required = false, example = "preview")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Excel文件流(.xlsx)", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
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
        if (StringUtils.isBlank(fileName)) {
            throw new ReportComputeException("Report file can not be null.");
        }
        OutputStream outputStream = resp.getOutputStream();
        try {
            String excelName = req.getParameter("_n");
            excelName = buildDownloadFileName(ReportConstants.MODE_KEY,excelName, ".xlsx");
            excelName = new String(excelName.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
            resp.setContentType("application/octet-stream;charset=ISO8859-1");
            resp.setHeader("Content-Disposition", "attachment;filename=\"" + excelName + "\"");
            Map<String, Object> parameters = buildParameters(req);
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
        } catch (Exception ex) {
            throw new ReportException("Export Excel failed, reportPath: " + fileName, ex);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                // ignore close error
            }
        }
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

    protected String buildDownloadFileName(String reportFileName, String fileName, String extName) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(fileName)) {
            fileName = decode(fileName);
            if (!fileName.toLowerCase().endsWith(extName)) {
                fileName = fileName + extName;
            }
            return fileName;
        } else {
            int pos = reportFileName.indexOf(":");
            if (pos > 0) {
                reportFileName = reportFileName.substring(pos + 1);
            }
            pos = reportFileName.toLowerCase().indexOf(".ureport.xml");
            if (pos > 0) {
                reportFileName = reportFileName.substring(0, pos);
            }
            return "ureport-" + reportFileName + extName;
        }
    }
}
