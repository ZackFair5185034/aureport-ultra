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
package com.aureport.ultra.core.expression.model.expr;

import com.aureport.ultra.core.Utils;
import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.exception.ReportComputeException;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectExpressionData;
import com.aureport.ultra.core.expression.model.data.ObjectListExpressionData;
import com.aureport.ultra.core.expression.model.expr.set.CellExpression;
import com.aureport.ultra.core.model.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年1月21日
 */
public class RelativeCellExpression extends CellExpression {
    private static final long serialVersionUID = 8826396779392348224L;

    public RelativeCellExpression(String cellName) {
        super(cellName);
    }

    @Override
    public boolean supportPaging() {
        return false;
    }

    @Override
    protected ExpressionData<?> compute(Cell cell, Cell currentCell, Context context) {
        List<Cell> targetCells = Utils.fetchTargetCells(currentCell, context, cellName);
        int size = targetCells.size();
        if (size == 0) {
            throw new ReportComputeException("Unknow cell " + cellName);
        } else if (size == 1) {
            return new ObjectExpressionData(targetCells.get(0).getData());
        } else {
            Cell targetCell = null;
            for (Cell c : targetCells) {
                if (c.getRow() == currentCell.getRow() || c.getColumn() == currentCell.getColumn()) {
                    targetCell = c;
                    break;
                }
            }
            if (targetCell != null) {
                return new ObjectExpressionData(targetCell.getData());
            } else {
                List<Object> list = new ArrayList<Object>();
                for (Cell c : targetCells) {
                    list.add(c.getData());
                }
                return new ObjectListExpressionData(list);
            }
        }
    }
}
