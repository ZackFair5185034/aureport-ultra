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
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express the implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.aureport.ultra.core.build.aggregate;

import com.aureport.ultra.core.Utils;
import com.aureport.ultra.core.build.BindData;
import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.definition.Order;
import com.aureport.ultra.core.expression.model.expr.dataset.DatasetExpression;
import com.aureport.ultra.core.model.Cell;
import com.aureport.ultra.core.utils.DataUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * iterate 聚合处理器
 *
 * 用法场景：父行对象中包含嵌套数组属性（如员工.familyMembers），
 * 需要为数组中每个元素展开生成一行。
 *
 * 与 group 的区别：
 * - group: 对整个数据集按 property 分组，每个分组值对应一行
 * - iterate: 对父行对象的 nestProperty 数组展开，每个数组元素生成一行
 *
 * 设计器配置：
 * - aggregate = iterate
 * - nestProperty = 嵌套数组属性路径（如 familyMembers）
 * - property = 展示字段（如 familyMembers.name 显示姓名列）
 *
 * @author Hermes Agent
 * @since 2026-05-14
 */
public class IterateAggregate extends Aggregate {

    @Override
    public List<BindData> aggregate(DatasetExpression expr, Cell cell, Context context) {
        // 获取当前单元格已有的绑定数据（父行对象）
        List<Object> parentDataList = cell.getBindData();
        if (parentDataList == null || parentDataList.isEmpty()) {
            // 没有父行数据，说明是顶层单元格，从数据集取
            parentDataList = (List<Object>) DataUtils.fetchData(cell, context, expr.getDatasetName());
        }

        String nestProperty = expr.getNestProperty();
        String property = expr.getProperty();
        // 防御：如果 property 带了 nestProperty 前缀（如 familyMembers.name），自动剥离
        if (property != null && nestProperty != null && property.startsWith(nestProperty + ".")) {
            property = property.substring(nestProperty.length() + 1);
        }
        List<BindData> result = new ArrayList<BindData>();

        if (parentDataList.isEmpty()) {
            result.add(new BindData(""));
            return result;
        }

        // 对每个父行对象的嵌套数组做迭代展开
        for (Object parentObj : parentDataList) {
            // 用 nestProperty 取出嵌套数组
            Object nestValue = Utils.getProperty(parentObj, nestProperty);

            if (nestValue == null) {
                continue;
            }

            Collection<?> nestItems;
            if (nestValue instanceof Collection) {
                nestItems = (Collection<?>) nestValue;
            } else if (nestValue instanceof Object[]) {
                nestItems = java.util.Arrays.asList((Object[]) nestValue);
            } else {
                // 不是集合类型，跳过
                continue;
            }

            for (Object item : nestItems) {
                // 对每个数组元素，提取 property 字段作为显示值
                Object displayValue = null;
                if (property != null && !property.isEmpty()) {
                    displayValue = Utils.getProperty(item, property);
                }

                // 该数组元素作为新的绑定数据传给下钻格
                List<Object> rowList = new ArrayList<Object>();
                rowList.add(item);

                if (displayValue != null) {
                    result.add(new BindData(displayValue, rowList));
                } else {
                    result.add(new BindData(item, rowList));
                }
            }
        }

        if (result.isEmpty()) {
            result.add(new BindData(""));
        }

        // 排序
        Order order = expr.getOrder();
        if (order != null && !order.equals(Order.none)) {
            orderBindDataList(result, order);
        }

        return result;
    }
}
