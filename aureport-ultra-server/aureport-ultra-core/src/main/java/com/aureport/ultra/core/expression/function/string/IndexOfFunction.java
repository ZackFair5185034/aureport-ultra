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
package com.aureport.ultra.core.expression.function.string;

import com.aureport.ultra.core.Utils;
import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.exception.ReportComputeException;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectExpressionData;
import com.aureport.ultra.core.model.Cell;

import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年1月24日
 */
public class IndexOfFunction extends StringFunction {

    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        String text = buildString(dataList);
        String targetText = null;
        if (dataList.size() > 1) {
            ExpressionData<?> exprData = dataList.get(1);
            if (exprData instanceof ObjectExpressionData) {
                ObjectExpressionData objData = (ObjectExpressionData) exprData;
                Object obj = objData.getData();
                if (obj == null) {
                    throw new ReportComputeException("Function [" + name() + "] parameter can not be null.");
                }
                targetText = obj.toString();
            }
        }
        int start = 0;
        if (dataList.size() == 3) {
            ExpressionData<?> exprData = dataList.get(2);
            start = buildStart(exprData);
        }
        return text.indexOf(targetText, start);
    }

    private int buildStart(ExpressionData<?> exprData) {
        if (exprData instanceof ObjectExpressionData) {
            ObjectExpressionData objData = (ObjectExpressionData) exprData;
            Object obj = objData.getData();
            if (obj == null) {
                throw new ReportComputeException("Function [" + name() + "] parameter can not be null.");
            }
            return Utils.toBigDecimal(obj).intValue();
        }
        throw new ReportComputeException("Function [" + name() + "] start position data is invalid : " + exprData);
    }

    @Override
    public String name() {
        return "indexof";
    }

}
