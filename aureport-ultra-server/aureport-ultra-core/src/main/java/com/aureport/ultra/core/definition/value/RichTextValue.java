/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package com.aureport.ultra.core.definition.value;

/**
 * 富文本单元格值
 * 支持HTML格式的富文本内容：加粗、斜体、颜色等
 *
 * @author 奥力给
 * @since 2026-05-17
 */
public class RichTextValue implements Value {
    private String content;

    public RichTextValue() {
    }

    public RichTextValue(String content) {
        this.content = content;
    }

    @Override
    public ValueType getType() {
        return ValueType.richtext;
    }

    @Override
    public String getValue() {
        return content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
