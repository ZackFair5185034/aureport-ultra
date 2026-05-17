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
package com.aureport.ultra.core.parser.impl.value;

import com.aureport.ultra.core.definition.value.ProgressBarValue;
import com.aureport.ultra.core.definition.value.Value;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

/**
 * 进度条值解析器
 *
 * @author 奥力给
 * @since 2026-05-17
 */
public class ProgressBarValueParser extends ValueParser {
    @Override
    public Value parse(Element element) {
        ProgressBarValue progressBarValue = new ProgressBarValue();
        
        String minValue = element.attributeValue("min-value");
        if (StringUtils.isNotBlank(minValue)) {
            progressBarValue.setMinValue(Integer.parseInt(minValue));
        }
        
        String maxValue = element.attributeValue("max-value");
        if (StringUtils.isNotBlank(maxValue)) {
            progressBarValue.setMaxValue(Integer.parseInt(maxValue));
        }
        
        String currentValue = element.attributeValue("current-value");
        if (StringUtils.isNotBlank(currentValue)) {
            progressBarValue.setCurrentValue(Integer.parseInt(currentValue));
        }
        
        String barColor = element.attributeValue("bar-color");
        if (StringUtils.isNotBlank(barColor)) {
            progressBarValue.setBarColor(barColor);
        }
        
        String bgColor = element.attributeValue("bg-color");
        if (StringUtils.isNotBlank(bgColor)) {
            progressBarValue.setBgColor(bgColor);
        }
        
        return progressBarValue;
    }
}