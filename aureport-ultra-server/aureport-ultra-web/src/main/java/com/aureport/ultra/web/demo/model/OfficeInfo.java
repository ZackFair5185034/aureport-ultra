package com.aureport.ultra.web.demo.model;

import com.aureport.ultra.core.annotation.FieldDesc;

import java.io.Serializable;

/**
 * 办公信息（嵌套在 Organization 中，包含嵌套 AddressInfo）
 */
public class OfficeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String buildingName;
    private String floor;
    private String roomNumber;
    private String area;
    private AddressInfo addressInfo;

    public OfficeInfo() {}

    @FieldDesc("楼宇名称")
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    @FieldDesc("楼层")
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    @FieldDesc("房间号")
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    @FieldDesc("面积(m²)")
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    @FieldDesc("详细地址")
    public AddressInfo getAddressInfo() { return addressInfo; }
    public void setAddressInfo(AddressInfo addressInfo) { this.addressInfo = addressInfo; }
}
