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
package com.aureport.ultra.core.expression.parse.builder;

import com.aureport.ultra.core.Utils;
import com.aureport.ultra.core.dsl.ReportParserParser.UnitContext;
import com.aureport.ultra.core.expression.model.expr.BaseExpression;
import com.aureport.ultra.core.expression.model.expr.NumberExpression;

import java.math.BigDecimal;

/**
 * @author Jacky.gao
 * @since 2016年12月25日
 */
public class NumberExpressionBuilder implements ExpressionBuilder {
    @Override
    public BaseExpression build(UnitContext unitContext) {
        BigDecimal value = Utils.toBigDecimal(unitContext.NUMBER().getText());
        return new NumberExpression(value);
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.NUMBER() != null;
    }
}
