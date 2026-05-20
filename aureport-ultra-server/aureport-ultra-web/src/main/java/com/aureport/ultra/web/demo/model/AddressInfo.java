package com.aureport.ultra.web.demo.model;

import com.aureport.ultra.core.annotation.FieldDesc;

import java.io.Serializable;

/**
 * 地址信息（叶子节点，嵌套在 OfficeInfo 中）
 */
public class AddressInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String province;
    private String city;
    private String district;
    private String street;
    private String zipCode;

    public AddressInfo() {}

    @FieldDesc("省份")
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    @FieldDesc("城市")
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    @FieldDesc("区县")
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    @FieldDesc("街道")
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    @FieldDesc("邮编")
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
}
