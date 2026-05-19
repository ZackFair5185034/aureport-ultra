package com.aureport.ultra.web.controller.designer;

import com.aureport.ultra.core.cache.CacheUtils;
import com.aureport.ultra.core.definition.ReportDefinition;
import com.aureport.ultra.core.dsl.ReportParserLexer;
import com.aureport.ultra.core.dsl.ReportParserParser;
import com.aureport.ultra.core.export.ReportRender;
import com.aureport.ultra.core.expression.ErrorInfo;
import com.aureport.ultra.core.expression.ScriptErrorListener;
import com.aureport.ultra.core.parser.ReportParser;
import com.aureport.ultra.core.provider.report.ReportProvider;
import com.aureport.ultra.web.cache.TempObjectCache;
import com.aureport.ultra.web.exception.ReportDesignException;
import com.aureport.ultra.web.filter.RequestHolderFilter;
import com.aureport.ultra.web.utils.ResponseUtils;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.*;

/**
 * 报表设计器控制器
 *
 * @author Jacky.gao
 * @since 2017年1月25日
 */
@Controller("bean.designerController")
@RequestMapping(value = "${aureport-ultra.servletPrefix}/designer", method = RequestMethod.GET)
@Tag(name = "报表设计器", description = "报表设计器脚本验证、文件管理与预览")
public class DesignerController implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(RequestHolderFilter.class);
    private final List<ReportProvider> reportProviders = new ArrayList<>();

    @Autowired
    private ReportRender reportRender;
    @Autowired
    private ReportParser reportParser;


    /**
     * 脚本验证
     */
    @Operation(
        summary = "脚本验证",
        parameters = {
            @Parameter(name = "content", description = "脚本内容", required = true, example = "dataset.id == '001'")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "验证结果JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/scriptValidation", method = RequestMethod.GET)
    public void scriptValidation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String content = req.getParameter("content");
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(content);
        ReportParserLexer lexer = new ReportParserLexer(antlrInputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReportParserParser parser = new ReportParserParser(tokenStream);
        ScriptErrorListener errorListener = new ScriptErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        parser.expression();
        List<ErrorInfo> infos = errorListener.getInfos();
        ResponseUtils.writeObjectToJson(resp, infos);
    }

    /**
     * 条件脚本验证
     */
    @Operation(
        summary = "条件脚本验证",
        parameters = {
            @Parameter(name = "content", description = "条件脚本内容", required = true, example = "id > 100")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "验证结果JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/conditionScriptValidation", method = RequestMethod.GET)
    public void conditionScriptValidation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String content = req.getParameter("content");
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(content);
        ReportParserLexer lexer = new ReportParserLexer(antlrInputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReportParserParser parser = new ReportParserParser(tokenStream);
        ScriptErrorListener errorListener = new ScriptErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        parser.expr();
        List<ErrorInfo> infos = errorListener.getInfos();
        ResponseUtils.writeObjectToJson(resp, infos);
    }

    /**
     * 解析数据集名称
     */
    @Operation(
        summary = "解析数据集名称",
        parameters = {
            @Parameter(name = "expr", description = "数据集表达式", required = true, example = "ds.id")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "解析结果JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/parseDatasetName", method = RequestMethod.GET)
    public void parseDatasetName(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String expr = req.getParameter("expr");
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(expr);
        ReportParserLexer lexer = new ReportParserLexer(antlrInputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReportParserParser parser = new ReportParserParser(tokenStream);
        parser.removeErrorListeners();
        ReportParserParser.DatasetContext ctx = parser.dataset();
        String datasetName = ctx.Identifier().getText();
        Map<String, String> result = new HashMap<String, String>();
        result.put("datasetName", datasetName);
        ResponseUtils.writeObjectToJson(resp, result);
    }


    /**
     * 保存预览文件
     */
    @Operation(
        summary = "保存预览文件",
        parameters = {
            @Parameter(name = "content", description = "报表文件内容(XML格式)", required = true, example = "<?xml version=\"1.0\"?>..."),
            @Parameter(name = "fileName", description = "报表文件名", required = true, example = "file:xxx.ureport.xml")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "保存成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/savePreviewFile", method = RequestMethod.GET)
    public void savePreviewFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String content = req.getParameter("content");
        String fileName = req.getParameter("fileName");
        content = decode(content);
        fileName = decode(fileName);
        InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
        ReportDefinition reportDef = reportParser.parse(inputStream, fileName);
        reportRender.rebuildReportDefinition(reportDef);
        IOUtils.closeQuietly(inputStream);
        TempObjectCache.putObject(fileName, reportDef);
    }

    /**
     * 加载报表
     */
    @Operation(
        summary = "加载报表",
        parameters = {
            @Parameter(name = "filePath", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "报表定义JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/loadReport", method = RequestMethod.GET)
    public void loadReport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String filePath = req.getParameter("filePath");
        if (filePath == null) {
            throw new ReportDesignException("Report file can not be null.");
        }
        String fileName = ReportUtils.decodeFileName(filePath);
        Object obj = TempObjectCache.getObject(fileName);
        if (obj instanceof ReportDefinition) {
            ReportDefinition reportDef = (ReportDefinition) obj;
            TempObjectCache.removeObject(fileName);
            ResponseUtils.writeObjectToJson(resp, new ReportDefinitionWrapper(reportDef));
        } else {
            ReportDefinition reportDef = reportRender.parseReport(fileName);
            ResponseUtils.writeObjectToJson(resp, new ReportDefinitionWrapper(reportDef));
        }
    }

    /**
     * 删除报表文件
     */
    @Operation(
        summary = "删除报表文件",
        parameters = {
            @Parameter(name = "file", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/deleteReportFile", method = RequestMethod.GET)
    public void deleteReportFile(HttpServletRequest req, HttpServletResponse resp) {
        String file = req.getParameter("file");
        if (file == null) {
            throw new ReportDesignException("Report file can not be null.");
        }
        ReportProvider targetReportProvider = null;
        for (ReportProvider provider : reportProviders) {
            if (file.startsWith(provider.getPrefix())) {
                targetReportProvider = provider;
                break;
            }
        }
        if (targetReportProvider == null) {
            throw new ReportDesignException("File [" + file + "] not found available report provider.");
        }
        targetReportProvider.deleteReport(file);
    }

    /**
     * 保存报表文件
     */
    @Operation(
        summary = "保存报表文件",
        parameters = {
            @Parameter(name = "file", description = "报表文件路径", required = true, example = "file:xxx.ureport.xml"),
            @Parameter(name = "content", description = "报表文件内容(XML格式)", required = true, example = "<?xml version=\"1.0\"?>...")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "保存成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/saveReportFile", method = RequestMethod.GET)
    public void saveReportFile(HttpServletRequest req, HttpServletResponse resp) {
        String file = req.getParameter("file");
        file = ReportUtils.decodeFileName(file);
        String content = req.getParameter("content");
        content = decode(content);
        ReportProvider targetReportProvider = null;
        for (ReportProvider provider : reportProviders) {
            if (file.startsWith(provider.getPrefix())) {
                targetReportProvider = provider;
                break;
            }
        }
        if (targetReportProvider == null) {
            throw new ReportDesignException("File [" + file + "] not found available report provider.");
        }
        ReportDefinition reportDef;
        try{
            InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
            reportDef = reportParser.parse(inputStream, file);
            IOUtils.closeQuietly(inputStream);
        }catch (Exception e){
            logger.error("保存报表异常",e);
            throw e;
        }
        reportRender.rebuildReportDefinition(reportDef);
        CacheUtils.cacheReportDefinition(file, reportDef);
        targetReportProvider.saveReport(file, content);
    }

    /**
     * 加载报表提供者
     */
    @Operation(
        summary = "加载报表提供者",
        parameters = {
            @Parameter(name = "path", description = "报表存储路径(可选)", required = false, example = "/reports")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "报表提供者列表JSON", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
        }
    )
    @RequestMapping(value = "/loadReportProviders", method = RequestMethod.GET)
    public void loadReportProviders(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getParameter("path");
        if (path == null || path.isEmpty()) {
            ResponseUtils.writeObjectToJson(resp, reportProviders);
        } else {
            Map<String, Object> result = new HashMap<>();
            for (ReportProvider provider : reportProviders) {
                if (provider.disabled() || provider.getName() == null) {
                    continue;
                }
                Map<String, Object> providerData = new HashMap<>();
                providerData.put("name", provider.getName());
                providerData.put("prefix", provider.getPrefix());
                providerData.put("disabled", provider.disabled());
                providerData.put("reportFiles", provider.getReportFiles(path));
                result.put(provider.getPrefix(), providerData);
            }
            ResponseUtils.writeObjectToJson(resp, result);
        }
    }

    /**
     * 解码内容
     */
    protected String decode(String content) {
        if (content == null) {
            return content;
        }
        try {
            content = URLDecoder.decode(content, "utf-8");
            return content;
        } catch (Exception ex) {
            return content;
        }
    }

    /**
     * 设置应用上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Collection<ReportProvider> providers = applicationContext.getBeansOfType(ReportProvider.class).values();
        for (ReportProvider provider : providers) {
            if (provider.disabled() || provider.getName() == null) {
                continue;
            }
            reportProviders.add(provider);
        }
    }

}
