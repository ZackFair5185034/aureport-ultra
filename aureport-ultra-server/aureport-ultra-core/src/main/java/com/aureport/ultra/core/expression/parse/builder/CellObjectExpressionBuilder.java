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

import com.aureport.ultra.core.dsl.ReportParserParser.CellContext;
import com.aureport.ultra.core.dsl.ReportParserParser.PropertyContext;
import com.aureport.ultra.core.dsl.ReportParserParser.UnitContext;
import com.aureport.ultra.core.expression.model.expr.BaseExpression;
import com.aureport.ultra.core.expression.model.expr.cell.CellObjectExpression;

/**
 * @author Jacky.gao
 * @since 2017年1月20日
 */
public class CellObjectExpressionBuilder implements ExpressionBuilder {

    @Override
    public BaseExpression build(UnitContext unitContext) {
        CellContext ctx = unitContext.cell();
        String property = null;
        PropertyContext propCtx = ctx.property();
        if (propCtx != null) {
            property = propCtx.getText();
        }
        CellObjectExpression expr = new CellObjectExpression(property);
        expr.setExpr(ctx.getText());
        return expr;
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.cell() != null;
    }
}
