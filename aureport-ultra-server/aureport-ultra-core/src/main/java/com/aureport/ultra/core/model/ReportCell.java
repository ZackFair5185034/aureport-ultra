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
package com.aureport.ultra.core.model;

import com.aureport.ultra.core.definition.CellStyle;
import com.aureport.ultra.core.definition.Expand;
import com.aureport.ultra.core.definition.value.Value;

import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年1月19日
 */
public interface ReportCell {
    CellStyle getCellStyle();

    String getName();

    int getRowSpan();

    int getColSpan();

    Row getRow();

    Column getColumn();

    Object getData();

    Value getValue();

    Expand getExpand();

    List<Object> getBindData();
}
