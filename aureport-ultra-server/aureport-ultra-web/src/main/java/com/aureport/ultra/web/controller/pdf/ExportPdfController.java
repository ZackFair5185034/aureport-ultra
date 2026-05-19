package com.aureport.ultra.web.controller.pdf;

import com.aureport.ultra.core.build.ReportBuilder;
import com.aureport.ultra.core.definition.Paper;
import com.aureport.ultra.core.definition.ReportDefinition;
import com.aureport.ultra.core.exception.ReportException;
import com.aureport.ultra.core.export.ExportConfigure;
import com.aureport.ultra.core.export.ExportConfigureImpl;
import com.aureport.ultra.core.export.ExportManager;
import com.aureport.ultra.core.export.ReportRender;
import com.aureport.ultra.core.export.pdf.PdfProducer;
import com.aureport.ultra.core.model.Report;
import com.aureport.ultra.web.cache.TempObjectCache;
import com.aureport.ultra.web.constant.ReportConstants;
import com.aureport.ultra.web.exception.ReportDesignException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * PDF导出控制器
 */
@RestController("bean.exportPdfController")
@RequestMapping(value = "${aureport-ultra.servletPrefix}/pdf", method = RequestMethod.GET)
@Tag(name = "PDF导出", description = "PDF格式报表导出与显示功能")
public class ExportPdfController {

    @Autowired
    private ReportBuilder reportBuilder;

    @Autowired
    private ExportManager exportManager;

    @Autowired
    private ReportRender reportRender;

    private final PdfProducer pdfProducer = new PdfProducer();

    /**
     * 构建PDF报表
     */
    @Operation(
        summary = "构建PDF报表",
        parameters = {
            @Parameter(name = "reportPath", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml"),
            @Parameter(name = "_n", description = "导出文件名(不含扩展名)", required = false, example = "report"),
            @Parameter(name = "mode", description = "预览模式(preview为预览)", required = false, example = "preview")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "PDF文件流(.pdf)", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/build", method = RequestMethod.GET)
    public void build(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        buildPdf(req, resp, false);
    }

    /**
     * 显示PDF报表
     */
    @Operation(
        summary = "显示PDF报表",
        parameters = {
            @Parameter(name = "reportPath", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml"),
            @Parameter(name = "_n", description = "导出文件名(不含扩展名)", required = false, example = "report"),
            @Parameter(name = "mode", description = "预览模式(preview为预览)", required = false, example = "preview")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "PDF文件流(.pdf)", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/show", method = RequestMethod.GET)
    public void show(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        buildPdf(req, resp, true);
    }

    @Operation(
        summary = "新分页PDF报表",
        parameters = {
            @Parameter(name = "reportPath", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml"),
            @Parameter(name = "_paper", description = "纸张配置JSON", required = false, example = "{\"paperType\":\"A4\",\"orientation\":\"landscape\"}"),
            @Parameter(name = "mode", description = "预览模式(preview为预览)", required = false, example = "preview")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "PDF文件流(.pdf)", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/newPaging", method = RequestMethod.GET)
    public void newPaging(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fileName = req.getParameter("reportPath");
        fileName = decode(fileName);
        String mode = req.getParameter("mode");
        boolean isPreview = ReportConstants.MODE_KEY.equals(mode);
        Report report;
        Map<String, Object> parameters = buildParameters(req);
        if (isPreview) {
            ReportDefinition reportDefinition = (ReportDefinition) TempObjectCache.getObject(fileName);
            if (reportDefinition == null) {
                throw new ReportDesignException("Report data has expired,can not do export pdf.");
            }
            report = reportBuilder.buildReport(reportDefinition, parameters);
        } else {
            ReportDefinition reportDefinition = reportRender.getReportDefinition(fileName);
            report = reportRender.render(reportDefinition, parameters);
        }
        String paper = req.getParameter("_paper");
        ObjectMapper mapper = new ObjectMapper();
        Paper newPaper = mapper.readValue(paper, Paper.class);
        report.rePaging(newPaper);
    }

    private void buildPdf(HttpServletRequest req, HttpServletResponse resp, boolean forPrint) throws IOException {
        String mode = req.getParameter("mode");
        boolean isPreview = ReportConstants.MODE_KEY.equals(mode);
        String fileName = req.getParameter("reportPath");
        fileName = decode(fileName);
        OutputStream outputStream = null;
        try {
            Map<String, Object> parameters = buildParameters(req);
            outputStream = resp.getOutputStream();
            if (forPrint) {
                resp.setContentType("application/pdf");
            } else {
                String pdfName = req.getParameter("_n");
                pdfName = buildDownloadFileName(ReportConstants.MODE_KEY, pdfName, ".pdf");
                pdfName = new String(pdfName.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
                resp.setContentType("application/octet-stream;charset=ISO8859-1");
                resp.setHeader("Content-Disposition", "attachment;filename=\"" + pdfName + "\"");
            }
            if (isPreview) {
                ReportDefinition reportDefinition = (ReportDefinition) TempObjectCache.getObject(fileName);
                if (reportDefinition == null) {
                    throw new ReportDesignException("Report data has expired,can not do export pdf.");
                }
                Report report = reportBuilder.buildReport(reportDefinition, parameters);
                pdfProducer.produce(report, outputStream);
            } else {
                ExportConfigure configure = new ExportConfigureImpl(fileName, parameters, outputStream);
                exportManager.exportPdf(configure);
            }
        } catch (Exception ex) {
            throw new ReportException("Export PDF failed, reportPath: " + fileName, ex);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // ignore close error
                }
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
