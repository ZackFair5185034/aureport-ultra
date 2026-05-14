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
package com.aureport.ultra.core.expression.model.expr.dataset;

import com.aureport.ultra.core.build.BindData;
import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.build.DatasetUtils;
import com.aureport.ultra.core.definition.Order;
import com.aureport.ultra.core.definition.mapping.MappingItem;
import com.aureport.ultra.core.definition.mapping.MappingType;
import com.aureport.ultra.core.definition.value.AggregateType;
import com.aureport.ultra.core.definition.value.GroupItem;
import com.aureport.ultra.core.expression.model.Condition;
import com.aureport.ultra.core.expression.model.data.BindDataListExpressionData;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.expression.model.expr.BaseExpression;
import com.aureport.ultra.core.model.Cell;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2016年11月18日
 */
public class DatasetExpression extends BaseExpression {
    private static final long serialVersionUID = -8794866509447790340L;
    private String datasetName;
    private AggregateType aggregate;
    private String property;
    /**
     * 嵌套属性路径，用于 iterate 聚合类型
     * 指定从父行对象中迭代展开的数组属性，如 familyMembers
     */
    private String nestProperty;
    /**
     * 当aggregate类型为自定义分组时，采用此属性来存储自定义分组各个项目
     */
    private List<GroupItem> groupItems;

    private MappingType mappingType = MappingType.simple;

    private String mappingDataset;
    private String mappingKeyProperty;
    private String mappingValueProperty;

    private List<MappingItem> mappingItems;

    @JsonIgnore
    private Condition condition;

    @JsonIgnore
    private Map<String, String> mapping = null;

    /**
     * 此属性给设计器使用，引擎不使用该属性
     */
    private List<Condition> conditions;
    private Order order;

    @Override
    public ExpressionData<?> compute(Cell cell, Cell currentCell, Context context) {
        List<BindData> bindDataList = DatasetUtils.computeDatasetExpression(this, cell, context);
        return new BindDataListExpressionData(bindDataList);
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public AggregateType getAggregate() {
        return aggregate;
    }

    public void setAggregate(AggregateType aggregate) {
        this.aggregate = aggregate;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getNestProperty() {
        return nestProperty;
    }

    public void setNestProperty(String nestProperty) {
        this.nestProperty = nestProperty;
    }

    public List<GroupItem> getGroupItems() {
        return groupItems;
    }

    public void setGroupItems(List<GroupItem> groupItems) {
        this.groupItems = groupItems;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<MappingItem> getMappingItems() {
        return mappingItems;
    }

    public void setMappingItems(List<MappingItem> mappingItems) {
        this.mappingItems = mappingItems;
        if (mappingItems != null) {
            mapping = new HashMap<String, String>();
            for (MappingItem item : mappingItems) {
                mapping.put(item.getValue(), item.getLabel());
            }
        }
    }

    public MappingType getMappingType() {
        return mappingType;
    }

    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }

    public String getMappingDataset() {
        return mappingDataset;
    }

    public void setMappingDataset(String mappingDataset) {
        this.mappingDataset = mappingDataset;
    }

    public String getMappingKeyProperty() {
        return mappingKeyProperty;
    }

    public void setMappingKeyProperty(String mappingKeyProperty) {
        this.mappingKeyProperty = mappingKeyProperty;
    }

    public String getMappingValueProperty() {
        return mappingValueProperty;
    }

    public void setMappingValueProperty(String mappingValueProperty) {
        this.mappingValueProperty = mappingValueProperty;
    }

    public Map<String, String> getMapping() {
        return mapping;
    }
}
