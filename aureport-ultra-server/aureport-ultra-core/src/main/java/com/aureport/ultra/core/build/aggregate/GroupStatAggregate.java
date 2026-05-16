/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.aureport.ultra.core.build.aggregate;

import com.aureport.ultra.core.Utils;
import com.aureport.ultra.core.build.BindData;
import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.expression.model.expr.dataset.DatasetExpression;
import com.aureport.ultra.core.model.Cell;
import com.aureport.ultra.core.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * GroupStatAggregate: 用于 group 单元格子格的统计聚合
 *
 * 场景：group 单元格（A3 group="dept"）展开后，子格需要显示该组的统计值
 * （如：应到人次、正常人次、异常人次）
 *
 * 与 SelectAggregate 的区别：
 * - SelectAggregate: 每个数据行返回一个 BindData（展开模式）
 * - GroupStatAggregate: 整个 group 的数据返回 1 个 BindData（统计模式）
 *
 * 用法：在子格中设置 aggregate="groupstat" property="dept_count"
 */
public class GroupStatAggregate extends Aggregate {
    @Override
    public List<BindData> aggregate(DatasetExpression expr, Cell cell, Context context) {
        // 从父格的 bindData 获取 group 的完整数据
        List<?> objList = DataUtils.fetchData(cell, context, expr.getDatasetName());
        List<BindData> result = new ArrayList<BindData>();

        if (objList == null || objList.isEmpty()) {
            result.add(new BindData(""));
            return result;
        }

        String property = expr.getProperty();

        // 只取第一行数据的 property 值（group 内所有行的统计值相同）
        Object firstObj = objList.get(0);
        Object data = Utils.getProperty(firstObj, property);

        // rowList 包含该 group 的所有行
        List<Object> rowList = new ArrayList<Object>();
        rowList.addAll(objList);

        result.add(new BindData(data, rowList));
        return result;
    }
}