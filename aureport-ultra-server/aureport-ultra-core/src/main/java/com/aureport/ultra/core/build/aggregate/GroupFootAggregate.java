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
import com.aureport.ultra.core.definition.value.AggregateType;
import com.aureport.ultra.core.expression.model.expr.dataset.DatasetExpression;
import com.aureport.ultra.core.model.Cell;
import com.aureport.ultra.core.utils.DataUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * GroupFootAggregate: 分组表尾单元格聚合处理器
 *
 * 场景：在分组行下方显示分组统计信息（如合计、平均值等）
 *
 * 设计器配置：
 * - aggregate = groupfoot
 * - property = 统计字段名（如 amount 计算该字段的统计值）
 * - subAggregate = sum/avg/count/max/min（子聚合类型，默认为 sum）
 *
 * @author Hermes Agent
 * @since 2026-05-17
 */
public class GroupFootAggregate extends Aggregate {
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
        String subAggregate = expr.getSubAggregate();
        if (subAggregate == null || subAggregate.isEmpty()) {
            subAggregate = "sum"; // 默认使用 sum
        }

        // 计算统计值
        Object statValue = computeStat(objList, property, subAggregate);

        // rowList 包含该 group 的所有行
        List<Object> rowList = new ArrayList<Object>();
        rowList.addAll(objList);

        result.add(new BindData(statValue, rowList));
        return result;
    }

    private Object computeStat(List<?> objList, String property, String subAggregate) {
        if (property == null || property.isEmpty()) {
            return "";
        }

        try {
            List<BigDecimal> values = new ArrayList<BigDecimal>();
            for (Object obj : objList) {
                Object propValue = Utils.getProperty(obj, property);
                if (propValue != null) {
                    BigDecimal bd = Utils.toBigDecimal(propValue);
                    if (bd != null) {
                        values.add(bd);
                    }
                }
            }

            if (values.isEmpty()) {
                return "";
            }

            switch (subAggregate.toLowerCase()) {
                case "sum":
                    BigDecimal sum = BigDecimal.ZERO;
                    for (BigDecimal v : values) {
                        sum = sum.add(v);
                    }
                    return sum;
                case "avg":
                    BigDecimal avgSum = BigDecimal.ZERO;
                    for (BigDecimal v : values) {
                        avgSum = avgSum.add(v);
                    }
                    return avgSum.divide(BigDecimal.valueOf(values.size()), 2, BigDecimal.ROUND_HALF_UP);
                case "count":
                    return BigDecimal.valueOf(values.size());
                case "max":
                    BigDecimal max = values.get(0);
                    for (BigDecimal v : values) {
                        if (v.compareTo(max) > 0) {
                            max = v;
                        }
                    }
                    return max;
                case "min":
                    BigDecimal min = values.get(0);
                    for (BigDecimal v : values) {
                        if (v.compareTo(min) < 0) {
                            min = v;
                        }
                    }
                    return min;
                default:
                    // 默认 sum
                    BigDecimal defaultSum = BigDecimal.ZERO;
                    for (BigDecimal v : values) {
                        defaultSum = defaultSum.add(v);
                    }
                    return defaultSum;
            }
        } catch (Exception e) {
            return "";
        }
    }
}
