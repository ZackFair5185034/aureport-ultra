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
package com.aureport.ultra.core.expression.function.math;

import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.model.Cell;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 求中位数
 *
 * @author Jacky.gao
 * @since 2017年1月23日
 */
public class MedianFunction extends MathFunction {

    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        List<BigDecimal> list = buildDataList(dataList);
        int size = list.size();
        if (size == 1) {
            return list.get(0);
        } else if (size == 2) {
            BigDecimal data = list.get(0).add(list.get(1));
            return data.divide(new BigDecimal(2), 8, BigDecimal.ROUND_HALF_UP);
        }
        Collections.sort(list);
        int mode = size % 2;
        if (mode == 0) {
            int half = size / 2;
            int start = half - 1;
            BigDecimal data = list.get(start).add(list.get(half));
            return data.divide(new BigDecimal(2), 8, BigDecimal.ROUND_HALF_UP);
        } else {
            int half = size / 2;
            return list.get(half);
        }
    }

    @Override
    public String name() {
        return "median";
    }
}
