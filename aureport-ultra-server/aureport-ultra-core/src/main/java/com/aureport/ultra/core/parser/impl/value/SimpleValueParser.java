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

import com.aureport.ultra.core.definition.value.SimpleValue;
import com.aureport.ultra.core.definition.value.Value;
import org.dom4j.Element;

/**
 * @author Jacky.gao
 * @since 2016年12月21日
 */
public class SimpleValueParser extends ValueParser {
    @Override
    public Value parse(Element element) {
        SimpleValue simpleValue = new SimpleValue(element.getText());
        return simpleValue;
    }
}
