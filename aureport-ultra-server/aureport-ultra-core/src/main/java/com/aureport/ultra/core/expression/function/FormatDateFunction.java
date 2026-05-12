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

import com.aureport.ultra.core.build.BindData;
import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.exception.ReportComputeException;
import com.aureport.ultra.core.expression.model.data.BindDataListExpressionData;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectListExpressionData;
import com.aureport.ultra.core.model.Cell;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年5月23日
 */
public class FormatDateFunction implements Function {
    private final String defaultPattern = "yyyy-MM-dd HH:mm:ss";
    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        if (dataList == null) {
            return "";
        }
        Object obj = null;
        String pattern = defaultPattern;
        for (ExpressionData<?> data : dataList) {
            if (data instanceof ObjectListExpressionData) {
                ObjectListExpressionData listExpressionData = (ObjectListExpressionData) data;
                List<?> list = listExpressionData.getData();
                if (!list.isEmpty()) {
                    obj = list.get(0);
                }
                if (list.size() > 1) {
                    pattern = list.get(1).toString();
                }
            } else if (data instanceof ObjectExpressionData) {
                obj = ((ObjectExpressionData) data).getData();
            } else if (data instanceof BindDataListExpressionData) {
                BindDataListExpressionData bindDataList = (BindDataListExpressionData) data;
                List<BindData> list = bindDataList.getData();
                if (!list.isEmpty()) {
                    obj = list.get(0).getValue();
                }
                if (list.size() > 1) {
                    pattern = list.get(1).getValue().toString();
                }
            }
        }
        if (obj == null) {
            throw new ReportComputeException("Function [formatdate] need a Date type parameter at least");
        } else {
            if (obj instanceof Date) {
                SimpleDateFormat sd = new SimpleDateFormat(pattern);
                return sd.format((Date) obj);
            } else {
                throw new ReportComputeException("Function [formatdate] first parameter is Date type");
            }
        }
    }

    @Override
    public String name() {
        return "formatdate";
    }
}

