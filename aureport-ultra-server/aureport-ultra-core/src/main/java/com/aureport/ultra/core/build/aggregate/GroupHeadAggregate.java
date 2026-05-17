/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
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
 * GroupHeadAggregate: 分组表头单元格聚合处理器
 *
 * 场景：在分组行上方显示分组名称/表头信息
 * 类似于 GroupStatAggregate，但用于表头位置
 *
 * 设计器配置：
 * - aggregate = grouphead
 * - property = 分组字段名（如 dept_name 显示"部门名称"）
 *
 * @author Hermes Agent
 * @since 2026-05-17
 */
public class GroupHeadAggregate extends Aggregate {
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

        // 获取分组的第一行数据的 property 值（作为表头）
        Object firstObj = objList.get(0);
        Object data = Utils.getProperty(firstObj, property);

        // rowList 包含该 group 的所有行
        List<Object> rowList = new ArrayList<Object>();
        rowList.addAll(objList);

        result.add(new BindData(data, rowList));
        return result;
    }
}
