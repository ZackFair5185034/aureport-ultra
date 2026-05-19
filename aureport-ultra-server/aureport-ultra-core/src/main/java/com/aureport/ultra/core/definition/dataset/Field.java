package com.aureport.ultra.core.definition.dataset;

import java.util.List;

/**
 * @author Jacky.gao
 * @since 2016年12月30日
 */
public class Field {
    private String name;
    private String label;
    private String type;
    private List<Field> children;

    public Field() {
    }

    public Field(String name) {
        this(name, null, null, null);
    }

    public Field(String name, String label, String type, List<Field> children) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Field> getChildren() {
        return children;
    }

    public void setChildren(List<Field> children) {
        this.children = children;
    }
}
