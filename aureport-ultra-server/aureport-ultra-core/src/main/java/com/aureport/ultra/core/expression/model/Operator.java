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
package com.aureport.ultra.core.expression.model;

import com.aureport.ultra.core.exception.ReportParseException;

/**
 * @author Jacky.gao
 * @since 2016年11月18日
 */
public enum Operator {
    Add, Subtract, Multiply, Divide, Complementation;

    public static Operator parse(String op) {
        if (op.equals("+")) {
            return Add;
        } else if (op.equals("-")) {
            return Subtract;
        } else if (op.equals("*")) {
            return Multiply;
        } else if (op.equals("/")) {
            return Divide;
        } else if (op.equals("%")) {
            return Complementation;
        } else {
            throw new ReportParseException("Unknow operator :" + op);
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case Add:
                return "+";
            case Divide:
                return "/";
            case Multiply:
                return "*";
            case Subtract:
                return "-";
            case Complementation:
                return "%";
        }
        throw new ReportParseException("Unknow operator: [" + this + "]");
    }

    ;
}
