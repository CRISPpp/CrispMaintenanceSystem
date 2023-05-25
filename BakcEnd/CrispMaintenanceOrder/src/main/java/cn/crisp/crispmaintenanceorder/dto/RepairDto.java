package cn.crisp.crispmaintenanceorder.dto;

import lombok.Data;

import java.util.List;

@SuppressWarnings({"all"})
/**
 * 维修成功 Dto
 */
@Data
public class RepairDto {
    /**
     * 订单 id
     */
    private Long indentId;

    /**
     * 图片
     */
    private List<String> photos;
}
