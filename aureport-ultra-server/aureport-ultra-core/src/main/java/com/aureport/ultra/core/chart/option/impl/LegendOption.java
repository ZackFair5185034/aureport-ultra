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
package com.aureport.ultra.core.chart.option.impl;

import com.aureport.ultra.core.chart.option.Labels;
import com.aureport.ultra.core.chart.option.Option;
import com.aureport.ultra.core.chart.option.Position;

/**
 * @author Jacky.gao
 * @since 2017年6月8日
 */
public class LegendOption implements Option {
    private boolean display = true;
    private Position position = Position.top;
    private Labels labels;

    @Override
    public String buildOptionJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"legend\":{");
        sb.append("\"display\":" + display + ",");
        sb.append("\"position\":\"" + position + "\"");
        if (labels != null) {
            sb.append("\"labels\":" + labels.toJson());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String getType() {
        return "legend";
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Labels getLabels() {
        return labels;
    }

    public void setLabels(Labels labels) {
        this.labels = labels;
    }
}
