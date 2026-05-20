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

import com.aureport.ultra.core.build.Dataset;
import com.aureport.ultra.core.exception.ReportComputeException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2016年12月27日
 */
public class BeanDatasetDefinition implements DatasetDefinition {
    private static final long serialVersionUID = -1332306988025304185L;
    private String name;
    private String method;
    private String clazz;
    private List<Field> fields;

    @SuppressWarnings("unchecked")
    public Dataset buildDataset(String datasourceName, Object obj, Map<String, Object> parameters) {
        try {
            Method m = obj.getClass().getMethod(method, new Class[]{String.class, String.class, Map.class});
            Object result = m.invoke(obj, new Object[]{datasourceName, name, parameters});
            List<Object> list;
            if (result instanceof List) {
                list = (List<Object>) result;
            } else {
                list = java.util.Collections.singletonList(result);
            }
            return new Dataset(name, list);
        } catch (Exception e) {
            throw new ReportComputeException(e);
        }
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
