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
package com.aureport.ultra.core.parser.impl;

import com.aureport.ultra.core.definition.ConditionPaging;
import com.aureport.ultra.core.definition.PagingPosition;
import com.aureport.ultra.core.parser.Parser;
import org.dom4j.Element;

/**
 * @author Jacky.gao
 * @since 2017年6月21日
 */
public class ConditionPagingParser implements Parser<ConditionPaging> {
    @Override
    public ConditionPaging parse(Element element) {
        ConditionPaging paging = new ConditionPaging();
        String position = element.attributeValue("position");
        paging.setPosition(PagingPosition.valueOf(position));
        String line = element.attributeValue("line");
        paging.setLine(Integer.valueOf(line));
        return paging;
    }
}
