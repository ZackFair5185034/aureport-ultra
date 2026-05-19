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
package com.aureport.ultra.core.definition.dataset;

import com.aureport.ultra.core.Utils;
import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.build.Dataset;
import com.aureport.ultra.core.definition.datasource.DataType;
import com.aureport.ultra.core.expression.ExpressionUtils;
import com.aureport.ultra.core.expression.model.Expression;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectExpressionData;
import com.aureport.ultra.core.utils.ProcedureUtils;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SqlDatasetDefinition implements DatasetDefinition {
    private static final long serialVersionUID = -1134526105416805870L;
    private String name;
    private String sql;
    private List<Parameter> parameters;
    private List<Field> fields;
    private Expression sqlExpression;
    private int queryTimeout;

    public Dataset buildDataset(Map<String, Object> parameterMap, Connection conn) {
        String sqlForUse = sql;
        Context context = new Context(null, parameterMap);
        if (sqlExpression != null) {
            sqlForUse = executeSqlExpr(sqlExpression, context);
        } else {
            Pattern pattern = Pattern.compile("\\$\\{.*?\\}");
            Matcher matcher = pattern.matcher(sqlForUse);
            while (matcher.find()) {
                String substr = matcher.group();
                String sqlExpr = substr.substring(2, substr.length() - 1);
                Expression expr = ExpressionUtils.parseExpression(sqlExpr);
                String result = executeSqlExpr(expr, context);
                sqlForUse = sqlForUse.replace(substr, result);
            }
        }
        Utils.logToConsole("RUNTIME SQL:" + sqlForUse);
        Map<String, Object> pmap = buildParameters(parameterMap);
        if (ProcedureUtils.isProcedure(sqlForUse)) {
            List<Map<String, Object>> result = ProcedureUtils.procedureQuery(sqlForUse, pmap, conn);
            return new Dataset(name, result);
        }
        SingleConnectionDataSource datasource = new SingleConnectionDataSource(conn, false);
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(datasource);
        final int timeout = this.queryTimeout;
        List<Map<String, Object>> list = jdbcTemplate.execute(sqlForUse, pmap,
            (PreparedStatementCallback<List<Map<String, Object>>>) ps -> {
                if (timeout > 0) {
                    ps.setQueryTimeout(timeout);
                }
                ResultSet rs = ps.executeQuery();
                List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
                ResultSetMetaData metaData = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<String, Object>();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        if (columnName == null || columnName.isEmpty()) {
                            columnName = metaData.getColumnName(i);
                        }
                        row.put(columnName, rs.getObject(i));
                    }
                    resultList.add(row);
                }
                rs.close();
                return resultList;
            });
        return new Dataset(name, list);
    }

    private String executeSqlExpr(Expression sqlExpr, Context context) {
        String sqlForUse = null;
        ExpressionData<?> exprData = sqlExpr.execute(null, null, context);
        if (exprData instanceof ObjectExpressionData) {
            ObjectExpressionData data = (ObjectExpressionData) exprData;
            Object obj = data.getData();
            if (obj != null) {
                String s = obj.toString();
                s = s.replaceAll("\\\\", "");
                sqlForUse = s;
            }
        }
        return sqlForUse;
    }


    private Map<String, Object> buildParameters(Map<String, Object> params) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Parameter param : parameters) {
            String name = param.getName();
            DataType datatype = param.getType();
            Object value = param.getDefaultValue();
            if (params != null && params.containsKey(name)) {
                value = params.get(name);
            }
            map.put(name, datatype.parse(value));
        }
        return map;
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void setSqlExpression(Expression sqlExpression) {
        this.sqlExpression = sqlExpression;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
}
