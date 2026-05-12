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
import com.aureport.ultra.core.model.Cell;
import com.aureport.ultra.core.model.Column;

import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年4月25日
 */
public class ColumnFunction implements Function {
    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        Column col = currentCell.getColumn();
        return col.getColumnNumber();
    }

    @Override
    public String name() {
        return "column";
    }
}
