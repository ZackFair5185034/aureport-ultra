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
package com.aureport.ultra.core.expression.function.date;

import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.exception.ReportComputeException;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectExpressionData;
import com.aureport.ultra.core.model.Cell;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年1月22日
 */
public class DateFunction extends CalendarFunction {
    private String pattern = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        SimpleDateFormat sd = new SimpleDateFormat(pattern);
        Date date = new Date();
        if (dataList.size() == 1) {
            ExpressionData<?> data = dataList.get(0);
            sd = buildPattern(data);
        }
        if (dataList.size() == 2) {
            Calendar c = buildCalendar(dataList);
            date = c.getTime();
            ExpressionData<?> data = dataList.get(1);
            sd = buildPattern(data);
        }
        return sd.format(date);
    }

    private SimpleDateFormat buildPattern(ExpressionData<?> data) {
        SimpleDateFormat sd = null;
        if (data instanceof ObjectExpressionData) {
            ObjectExpressionData objectData = (ObjectExpressionData) data;
            String newPattern = (String) objectData.getData();
            sd = new SimpleDateFormat(newPattern);
        } else {
            throw new ReportComputeException("Unknow date format pattern:" + data.getData());
        }
        return sd;
    }

    @Override
    public String name() {
        return "date";
    }
}
