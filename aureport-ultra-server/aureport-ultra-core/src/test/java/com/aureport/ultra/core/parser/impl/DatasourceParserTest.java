package com.aureport.ultra.core.parser.impl;

import com.aureport.ultra.core.definition.dataset.HttpDatasetDefinition;
import com.aureport.ultra.core.definition.datasource.DatasourceDefinition;
import com.aureport.ultra.core.definition.datasource.HttpDatasourceDefinition;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DatasourceParser 单元测试 —— 验证 HTTP 数据源中 beanId / beanMethod XML 属性解析。 
 */
class DatasourceParserTest {

    private final DatasourceParser parser = new DatasourceParser();

    @Test
    void testParseHttpDatasource_withBeanIdAndMethod() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<datasource type=\"http\" name=\"testHttp\" protocol=\"standard\""
            + "        hostType=\"manual\" host=\"http://localhost:8080\">"
            + "    <dataset name=\"users\" type=\"http\" url=\"/report/datasource/invoke\""
            + "            method=\"POST\" beanId=\"userService\" beanMethod=\"queryUsers\">"
            + "        <field name=\"id\"/>"
            + "        <field name=\"name\"/>"
            + "    </dataset>"
            + "</datasource>";

        Document doc = DocumentHelper.parseText(xml);
        Element root = doc.getRootElement();

        DatasourceDefinition def = parser.parse(root);
        assertNotNull(def);
        assertInstanceOf(HttpDatasourceDefinition.class, def);

        HttpDatasourceDefinition httpDs = (HttpDatasourceDefinition) def;
        assertEquals("testHttp", httpDs.getName());
        assertEquals("standard", httpDs.getProtocolType());
        assertEquals("manual", httpDs.getHostType());
        assertEquals("http://localhost:8080", httpDs.getHost());

        List<?> datasets = httpDs.getDatasets();
        assertNotNull(datasets);
        assertEquals(1, datasets.size());
        assertInstanceOf(HttpDatasetDefinition.class, datasets.get(0));

        HttpDatasetDefinition dataset = (HttpDatasetDefinition) datasets.get(0);
        assertEquals("users", dataset.getName());
        assertEquals("/report/datasource/invoke", dataset.getUrl());
        assertEquals("POST", dataset.getMethod());
        assertEquals("userService", dataset.getBeanId());
        assertEquals("queryUsers", dataset.getBeanMethod());
    }

    @Test
    void testParseHttpDatasource_withoutBeanId() throws Exception {
        // 旧格式或无 beanId 的 XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<datasource type=\"http\" name=\"oldHttp\" protocol=\"thirdParty\">"
            + "    <dataset name=\"orders\" type=\"http\""
            + "            url=\"http://api.example.com/orders\""
            + "            method=\"GET\" headers=\"{}\" responsePath=\"$.data\">"
            + "        <field name=\"orderId\"/>"
            + "    </dataset>"
            + "</datasource>";

        Document doc = DocumentHelper.parseText(xml);
        Element root = doc.getRootElement();

        DatasourceDefinition def = parser.parse(root);
        assertNotNull(def);
        assertInstanceOf(HttpDatasourceDefinition.class, def);

        HttpDatasetDefinition dataset = (HttpDatasetDefinition) ((HttpDatasourceDefinition) def).getDatasets().get(0);
        assertEquals("orders", dataset.getName());
        assertNull(dataset.getBeanId());
        assertNull(dataset.getBeanMethod());
        assertEquals("http://api.example.com/orders", dataset.getUrl());
        assertEquals("GET", dataset.getMethod());
        assertEquals("$.data", dataset.getResponsePath());
    }

    @Test
    void testParseHttpDatasource_thirdPartyDataset() throws Exception {
        // 三方协议：有 body、responsePath，但没有 beanId
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<datasource type=\"http\" name=\"external\" protocol=\"thirdParty\">"
            + "    <dataset name=\"users\" type=\"http\" url=\"http://api.example.com/v1/users\""
            + "            method=\"POST\" body=\"{}\" responsePath=\"$.data.items\""
            + "            headers=\"{}\">"
            + "        <field name=\"id\"/>"
            + "        <field name=\"name\"/>"
            + "    </dataset>"
            + "</datasource>";

        Document doc = DocumentHelper.parseText(xml);
        Element root = doc.getRootElement();

        DatasourceDefinition def = parser.parse(root);
        assertNotNull(def);
        HttpDatasetDefinition dataset = (HttpDatasetDefinition) ((HttpDatasourceDefinition) def).getDatasets().get(0);

        assertEquals("users", dataset.getName());
        assertEquals("http://api.example.com/v1/users", dataset.getUrl());
        assertEquals("POST", dataset.getMethod());
        assertEquals("{}", dataset.getBody());
        assertNull(dataset.getBeanId());
        assertNull(dataset.getBeanMethod());
        assertEquals("$.data.items", dataset.getResponsePath());
    }

    @Test
    void testParseHttpDatasource_withBeanIdOnly() throws Exception {
        // 只有 beanId 没有 beanMethod
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<datasource type=\"http\" name=\"test\" protocol=\"standard\">"
            + "    <dataset name=\"test\" type=\"http\" beanId=\"testService\">"
            + "        <field name=\"id\"/>"
            + "    </dataset>"
            + "</datasource>";

        Document doc = DocumentHelper.parseText(xml);
        Element root = doc.getRootElement();

        DatasourceDefinition def = parser.parse(root);
        assertNotNull(def);

        HttpDatasetDefinition dataset = (HttpDatasetDefinition) ((HttpDatasourceDefinition) def).getDatasets().get(0);
        assertEquals("testService", dataset.getBeanId());
        assertNull(dataset.getBeanMethod());
    }

    @Test
    void testParseSpringDatasource_beanAttribute() throws Exception {
        // Spring 数据源：验证 bean 属性解析不受到影响
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<datasource type=\"spring\" name=\"springDS\" bean=\"springUserService\">"
            + "    <dataset name=\"list\" type=\"bean\" method=\"getList\" clazz=\"com.example.User\">"
            + "        <field name=\"id\"/>"
            + "    </dataset>"
            + "</datasource>";

        Document doc = DocumentHelper.parseText(xml);
        Element root = doc.getRootElement();

        DatasourceDefinition def = parser.parse(root);
        assertNotNull(def);
        assertEquals("springDS", def.getName());

        // 验证 spring 数据源中 beanId 不为 null
        List<?> datasets = def.getDatasets();
        assertNotNull(datasets);
        assertEquals(1, datasets.size());
    }

    @Test
    void testParseHttpDatasource_withRequestParameters() throws Exception {
        // 标准协议数据集 + 请求参数
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<datasource type=\"http\" name=\"withParams\" protocol=\"standard\">"
            + "    <dataset name=\"users\" type=\"http\" url=\"/invoke\""
            + "            beanId=\"userService\" beanMethod=\"queryUsers\">"
            + "        <field name=\"id\"/>"
            + "        <field name=\"name\"/>"
            + "        <requestParameter name=\"page\" value=\"1\"/>"
            + "        <requestParameter name=\"size\" value=\"20\"/>"
            + "    </dataset>"
            + "</datasource>";

        Document doc = DocumentHelper.parseText(xml);
        Element root = doc.getRootElement();

        DatasourceDefinition def = parser.parse(root);
        assertNotNull(def);
        HttpDatasetDefinition dataset = (HttpDatasetDefinition) ((HttpDatasourceDefinition) def).getDatasets().get(0);

        assertEquals("userService", dataset.getBeanId());
        assertEquals("queryUsers", dataset.getBeanMethod());
        assertNotNull(dataset.getRequestParameters());
        assertEquals(2, dataset.getRequestParameters().size());
        assertEquals("page", dataset.getRequestParameters().get(0).getName());
        assertEquals("1", dataset.getRequestParameters().get(0).getValue());
        assertEquals("size", dataset.getRequestParameters().get(1).getName());
        assertEquals("20", dataset.getRequestParameters().get(1).getValue());
    }
}
