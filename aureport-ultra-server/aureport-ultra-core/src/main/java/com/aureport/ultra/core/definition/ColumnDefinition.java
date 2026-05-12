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
package com.aureport.ultra.core.definition;

import com.aureport.ultra.core.model.Column;

import java.util.List;

/**
 * @author Jacky.gao
 * @since 2016年11月1日
 */
public class ColumnDefinition implements Comparable<ColumnDefinition> {
    private int columnNumber;
    private int width;
    private boolean hide;

    protected Column newColumn(List<Column> columns) {
        Column col = new Column(columns);
        col.setWidth(width);
        return col;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    @Override
    public int compareTo(ColumnDefinition o) {
        return columnNumber - o.getColumnNumber();
    }
}
