/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package com.aureport.ultra.core.build.compute;

import com.aureport.ultra.core.build.BindData;
import com.aureport.ultra.core.build.Context;
import com.aureport.ultra.core.definition.value.ProgressBarValue;
import com.aureport.ultra.core.definition.value.ValueType;
import com.aureport.ultra.core.model.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * 进度条值计算器
 *
 * @author 奥力给
 * @since 2026-05-17
 */
public class ProgressBarValueCompute implements ValueCompute {

    @Override
    public List<BindData> compute(Cell cell, Context context) {
        List<BindData> list = new ArrayList<BindData>();
        ProgressBarValue progressBarValue = (ProgressBarValue) cell.getValue();
        list.add(new BindData(progressBarValue, null, null));
        return list;
    }

    @Override
    public ValueType type() {
        return ValueType.progressbar;
    }
}