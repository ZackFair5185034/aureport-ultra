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
package com.aureport.ultra.core.expression.function.page;

import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.model.Cell;

import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年5月5日
 */
public class PageRowsFunction extends PageFunction {

    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        int pageIndex = currentCell.getRow().getPageIndex();
        if (pageIndex == 0) pageIndex = 1;
        return context.getCurrentPageRows(pageIndex).size();
    }

    @Override
    public String name() {
        return "prows";
    }
}
