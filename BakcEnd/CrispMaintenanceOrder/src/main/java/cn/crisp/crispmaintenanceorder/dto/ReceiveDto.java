package cn.crisp.crispmaintenanceorder.dto;

import lombok.Data;

@SuppressWarnings({"all"})
/**
 * 接单 Dto
 */
@Data
public class ReceiveDto {
    /**
     * 订单 id
     */
    private Long indentId;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;
}