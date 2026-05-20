package com.aureport.ultra.web.demo.model;

import com.aureport.ultra.core.annotation.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 办公信息（嵌套在 Organization 中，包含嵌套 AddressInfo）
 */
@Data
@NoArgsConstructor
public class OfficeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @FieldDesc("楼宇名称")
    private String buildingName;

    @FieldDesc("楼层")
    private String floor;

    @FieldDesc("房间号")
    private String roomNumber;

    @FieldDesc("面积(m²)")
    private String area;

    @FieldDesc("详细地址")
    private AddressInfo addressInfo;
}
