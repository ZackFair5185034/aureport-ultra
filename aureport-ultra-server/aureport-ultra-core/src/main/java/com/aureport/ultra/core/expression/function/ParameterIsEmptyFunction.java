package com.aureport.ultra.core.expression.function;

import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.expression.model.data.ExpressionData;
import com.aureport.ultra.core.model.Cell;

import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年12月7日
 */
public class ParameterIsEmptyFunction extends ParameterFunction {
    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context,
                          Cell currentCell) {
        Object obj = super.execute(dataList, context, currentCell);
        if (obj == null || obj.toString().trim().equals("")) {
            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "emptyparam";
    }
}
