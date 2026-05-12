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
package com.aureport.ultra.core.chart.axes.impl;

import com.aureport.ultra.core.chart.axes.BaseAxes;
import com.aureport.ultra.core.chart.axes.ScaleLabel;
import com.aureport.ultra.core.chart.axes.YPosition;

/**
 * @author Jacky.gao
 * @since 2017年6月14日
 */
public class YAxes extends BaseAxes {
    private YPosition yposition;

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"ticks\":{");
        sb.append("\"minRotation\":" + getRotation() + "");
        sb.append("}");
        ScaleLabel scaleLabel = getScaleLabel();
        if (scaleLabel != null) {

            sb.append(",\"scaleLabel\":" + scaleLabel.toJson());
        }
        sb.append("}");
        return sb.toString();
    }

    public YPosition getYposition() {
        return yposition;
    }

    public void setYposition(YPosition yposition) {
        this.yposition = yposition;
    }
}
