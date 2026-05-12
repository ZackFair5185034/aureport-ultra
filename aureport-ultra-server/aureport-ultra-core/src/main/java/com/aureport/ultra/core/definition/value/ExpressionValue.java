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
package com.aureport.ultra.core.definition.value;

import com.aureport.ultra.core.expression.ExpressionUtils;
import com.aureport.ultra.core.expression.model.Expression;

/**
 * @author Jacky.gao
 * @since 2016年12月24日
 */
public class ExpressionValue implements Value {
    private String text;
    private Expression expression;

    public ExpressionValue(String text) {
        this.text = text;
        expression = ExpressionUtils.parseExpression(text);
    }

    @Override
    public ValueType getType() {
        return ValueType.expression;
    }

    @Override
    public String getValue() {
        return text;
    }

    public Expression getExpression() {
        return expression;
    }
}
