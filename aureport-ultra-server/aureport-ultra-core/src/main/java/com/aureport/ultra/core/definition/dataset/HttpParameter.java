package com.aureport.ultra.core.definition.dataset;

import java.io.Serializable;

/**
 * HTTP 请求参数键值对。
 */
public class HttpParameter implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    public HttpParameter() {}

    public HttpParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
