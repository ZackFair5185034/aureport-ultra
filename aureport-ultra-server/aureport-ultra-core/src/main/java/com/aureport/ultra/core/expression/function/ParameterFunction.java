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
package com.aureport.ultra.core.expression.function;

import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.exception.ReportComputeException;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectListExpressionData;
import com.aureport.ultra.core.model.Cell;

import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2017年5月21日
 */
public class ParameterFunction implements Function {
    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        if (dataList == null || dataList.isEmpty()) {
            throw new ReportComputeException("Function [param] need one parameter.");
        }
        Object obj = null;
        ExpressionData<?> data = dataList.get(0);
        if (data instanceof ObjectExpressionData) {
            ObjectExpressionData objData = (ObjectExpressionData) data;
            obj = objData.getData();
        } else if (data instanceof ObjectListExpressionData) {
            ObjectListExpressionData listData = (ObjectListExpressionData) data;
            List<?> list = listData.getData();
            if (!list.isEmpty()) {
                obj = list.get(0);
            }
        }
        if (obj == null) {
            throw new ReportComputeException("Function [param] need one parameter.");
        }
        Map<String, Object> map = context.getParameters();
        if (map == null) {
            return null;
        }
        return map.get(obj.toString());
    }
    @Override
    public String name() {
        return "param";
    }
}
