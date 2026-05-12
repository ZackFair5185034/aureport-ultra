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
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectListExpressionData;
import com.aureport.ultra.core.model.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年1月3日
 */
public class ListFunction implements Function {

    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        List<Object> list = new ArrayList<Object>();
        for (ExpressionData<?> d : dataList) {
            if (d instanceof ObjectExpressionData) {
                ObjectExpressionData exprData = (ObjectExpressionData) d;
                list.add(exprData.getData());
            } else if (d instanceof ObjectListExpressionData) {
                ObjectListExpressionData listData = (ObjectListExpressionData) d;
                list.addAll(listData.getData());
            }
        }
        return list;
    }

    @Override
    public String name() {
        return "list";
    }
}
