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
package com.aureport.ultra.core.parser.impl.value;

import com.aureport.ultra.core.definition.value.Source;
import com.aureport.ultra.core.definition.value.Value;
import com.aureport.ultra.core.definition.value.ZxingCategory;
import com.aureport.ultra.core.definition.value.ZxingValue;
import com.aureport.ultra.core.expression.ExpressionUtils;
import com.aureport.ultra.core.expression.model.Expression;
import org.dom4j.Element;

/**
 * @author Jacky.gao
 * @since 2017年3月6日
 */
public class ZxingValueParser extends ValueParser {

    @Override
    public Value parse(Element element) {
        ZxingValue value = new ZxingValue();
        Source source = Source.valueOf(element.attributeValue("source"));
        value.setSource(source);
        value.setWidth(Integer.valueOf(element.attributeValue("width")));
        value.setHeight(Integer.valueOf(element.attributeValue("height")));
        value.setFormat(element.attributeValue("format"));
        value.setCategory(ZxingCategory.valueOf(element.attributeValue("category")));
        value.setCodeDisplay(Boolean.valueOf(element.attributeValue("code-display")));
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (ele.getName().equals("text")) {
                if (source.equals(Source.text)) {
                    value.setText(ele.getText());
                } else {
                    value.setExpr(ele.getText());
                }
                break;
            }
        }
        if (source.equals(Source.expression)) {
            String expr = value.getExpr();
            Expression expression = ExpressionUtils.parseExpression(expr);
            value.setExpression(expression);
        }
        return value;
    }
}
