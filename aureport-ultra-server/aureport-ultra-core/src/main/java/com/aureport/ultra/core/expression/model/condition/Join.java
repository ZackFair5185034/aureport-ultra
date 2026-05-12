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
package com.aureport.ultra.core.expression.model.condition;

import com.aureport.ultra.core.exception.ReportParseException;

/**
 * @author Jacky.gao
 * @since 2016年12月1日
 */
public enum Join {
    and, or;

    public static Join parse(String join) {
        if (join.equals("and") || join.equals("&&")) {
            return and;
        }
        if (join.equals("or") || join.equals("||")) {
            return or;
        }
        throw new ReportParseException("Unknow join : " + join);
    }
}
