package com.aureport.ultra.web.demo.model;

import com.aureport.ultra.core.annotation.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 地址信息（叶子节点，嵌套在 OfficeInfo 中）
 */
@Data
@NoArgsConstructor
public class AddressInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @FieldDesc("省份")
    private String province;

    @FieldDesc("城市")
    private String city;

    @FieldDesc("区县")
    private String district;

    @FieldDesc("街道")
    private String street;

    @FieldDesc("邮编")
    private String zipCode;
}
