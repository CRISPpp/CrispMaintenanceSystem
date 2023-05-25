package cn.crisp.crispmaintenanceorder.dto;

import lombok.Data;

@SuppressWarnings({"all"})
@Data
public class DispatchDto {
    /**
     * 一页最多几个数据
     */
    private Integer size = 10;

    /**
     * 获取第几页，从第 0 页开始
     */
    private Integer current = 0;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 公里数
     */
    private Double dist;
}