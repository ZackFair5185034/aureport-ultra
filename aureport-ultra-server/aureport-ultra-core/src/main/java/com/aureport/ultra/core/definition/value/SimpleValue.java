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

/**
 * 普通字符串，或者是表达式
 *
 * @author Jacky.gao
 * @since 2016年11月1日
 */
public class SimpleValue implements Value {
    private String value;

    public SimpleValue(String value) {
        this.value = value;
    }

    @Override
    public ValueType getType() {
        return ValueType.simple;
    }

    @Override
    public String getValue() {
        return value;
    }
}
