/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.aureport.ultra.core.parser.impl;

import com.aureport.ultra.core.definition.dataset.BeanDatasetDefinition;
import com.aureport.ultra.core.definition.dataset.DatasetDefinition;
import com.aureport.ultra.core.definition.dataset.Field;
import com.aureport.ultra.core.definition.dataset.HttpDatasetDefinition;
import com.aureport.ultra.core.definition.dataset.HttpParameter;
import com.aureport.ultra.core.definition.dataset.Parameter;
import com.aureport.ultra.core.definition.dataset.SqlDatasetDefinition;
import com.aureport.ultra.core.definition.datasource.BuildinDatasourceDefinition;
import com.aureport.ultra.core.definition.datasource.DatasourceDefinition;
import com.aureport.ultra.core.definition.datasource.HttpDatasourceDefinition;
import com.aureport.ultra.core.definition.datasource.JdbcDatasourceDefinition;
import com.aureport.ultra.core.definition.datasource.SpringBeanDatasourceDefinition;
import com.aureport.ultra.core.definition.datasource.DataType;
import com.aureport.ultra.core.expression.ExpressionUtils;
import com.aureport.ultra.core.expression.model.Expression;
import com.aureport.ultra.core.parser.Parser;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2016年12月30日
 */
public class DatasourceParser implements Parser<DatasourceDefinition> {
    @Override
    public DatasourceDefinition parse(Element element) {
        String type = element.attributeValue("type");
        if (type.equals("jdbc")) {
            JdbcDatasourceDefinition ds = new JdbcDatasourceDefinition();
            ds.setName(element.attributeValue("name"));
            ds.setDriver(element.attributeValue("driver"));
            ds.setUrl(element.attributeValue("url"));
            ds.setUsername(element.attributeValue("username"));
            ds.setPassword(element.attributeValue("password"));
            ds.setDatasets(parseDatasets(element));
            return ds;
        } else if (type.equals("spring")) {
            SpringBeanDatasourceDefinition ds = new SpringBeanDatasourceDefinition();
            ds.setName(element.attributeValue("name"));
            ds.setBeanId(element.attributeValue("bean"));
            ds.setDatasets(parseDatasets(element));
            return ds;
        } else if (type.equals("buildin")) {
            BuildinDatasourceDefinition ds = new BuildinDatasourceDefinition();
            ds.setName(element.attributeValue("name"));
            ds.setDatasets(parseDatasets(element));
            return ds;
        } else if (type.equals("http")) {
            HttpDatasourceDefinition ds = new HttpDatasourceDefinition();
            ds.setName(element.attributeValue("name"));
            String protocol = element.attributeValue("protocol");
            if (protocol != null) {
                ds.setProtocolType(protocol);
            }
            String baseUrl = element.attributeValue("baseUrl");
            if (baseUrl != null) {
                ds.setBaseUrl(baseUrl);
            }
            String hostType = element.attributeValue("hostType");
            if (hostType != null) {
                ds.setHostType(hostType);
            }
            String host = element.attributeValue("host");
            if (host != null) {
                ds.setHost(host);
            }
            String serviceName = element.attributeValue("serviceName");
            if (serviceName != null) {
                ds.setServiceName(serviceName);
            }
            ds.setDatasets(parseDatasets(element));
            return ds;
        }
        return null;
    }

    private List<DatasetDefinition> parseDatasets(Element element) {
        List<DatasetDefinition> list = new ArrayList<DatasetDefinition>();
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            String type = ele.attributeValue("type");
            if (type.equals("sql")) {
                SqlDatasetDefinition dataset = new SqlDatasetDefinition();
                dataset.setName(ele.attributeValue("name"));
                dataset.setSql(parseSql(ele, dataset));
                dataset.setFields(parseFields(ele));
                dataset.setParameters(parseParameters(ele));
                list.add(dataset);
            } else if (type.equals("bean")) {
                BeanDatasetDefinition dataset = new BeanDatasetDefinition();
                dataset.setName(ele.attributeValue("name"));
                dataset.setMethod(ele.attributeValue("method"));
                dataset.setFields(parseFields(ele));
                dataset.setClazz(ele.attributeValue("clazz"));
                list.add(dataset);
            } else if (type.equals("http")) {
                HttpDatasetDefinition dataset = new HttpDatasetDefinition();
                dataset.setName(ele.attributeValue("name"));
                String url = ele.attributeValue("url");
                if (url != null) dataset.setUrl(url);
                String method = ele.attributeValue("method");
                if (method != null) dataset.setMethod(method);
                String headers = ele.attributeValue("headers");
                if (headers != null) dataset.setHeaders(headers);
                String body = ele.attributeValue("body");
                if (body != null) dataset.setBody(body);
                String responsePath = ele.attributeValue("responsePath");
                if (responsePath != null) dataset.setResponsePath(responsePath);
                dataset.setFields(parseFields(ele));
                dataset.setRequestParameters(parseHttpParameters(ele));
                list.add(dataset);
            }
        }
        return list;
    }

    private List<Parameter> parseParameters(Element element) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (!ele.getName().equals("parameter")) {
                continue;
            }
            Parameter param = new Parameter();
            param.setName(ele.attributeValue("name"));
            param.setDefaultValue(ele.attributeValue("default-value"));
            param.setType(DataType.valueOf(ele.attributeValue("type")));
            parameters.add(param);
        }
        return parameters;
    }

    private List<Field> parseFields(Element element) {
        List<Field> fields = new ArrayList<Field>();
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (!ele.getName().equals("field")) {
                continue;
            }
            Field field = new Field(ele.attributeValue("name"));
            String label = ele.attributeValue("label");
            if (label != null) {
                field.setLabel(label);
            }
            List<Field> children = parseFields(ele);
            if (!children.isEmpty()) {
                field.setChildren(children);
            }
            fields.add(field);
        }
        return fields;
    }

    private List<HttpParameter> parseHttpParameters(Element element) {
        List<HttpParameter> params = new ArrayList<>();
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (!ele.getName().equals("requestParameter")) {
                continue;
            }
            String name = ele.attributeValue("name");
            String value = ele.attributeValue("value");
            if (name != null) {
                params.add(new HttpParameter(name, value));
            }
        }
        return params;
    }

    private String parseSql(Element element, SqlDatasetDefinition dataset) {
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (ele.getName().equals("sql")) {
                String sql = ele.getText().trim();
                if (sql.startsWith(ExpressionUtils.EXPR_PREFIX) && sql.endsWith(ExpressionUtils.EXPR_SUFFIX)) {
                    String s = sql.substring(2, sql.length() - 1);
                    Expression expr = ExpressionUtils.parseExpression(s);
                    dataset.setSqlExpression(expr);
                }
                return ele.getText();
            }
        }
        return null;
    }
}
