package cn.crisp.crispmaintenanceorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@SuppressWarnings({"all"})
/**
 * 评价 Dto
 */
@Data
public class EvaluateDto {
    /**
     * 订单 id
     */
    private Long indentId;

    /**
     * 评分
     */
    private BigDecimal quality;
}