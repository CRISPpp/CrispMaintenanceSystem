package cn.crisp.crispmaintenanceorder.vo;

import cn.crisp.entity.Indent;
import cn.crisp.entity.IndentImage;
import cn.crisp.entity.User;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings({"all"})
@Data
public class IndentVo extends Indent {
    /**
     * 用户对象
     */
    private User user;

    /**
     * 工程师对象
     */
    private User engineer;

    /**
     * 工程师星级
     */
    private BigDecimal engineerQuality;

    /**
     * 订单图片
     */
    private List<IndentImage> images;

}