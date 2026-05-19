package com.aureport.ultra.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段描述注解，用于标注 Bean 属性的中文名称或描述。
 * 可标注在 getter 方法或字段上。
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldDesc {
    String value();
}
