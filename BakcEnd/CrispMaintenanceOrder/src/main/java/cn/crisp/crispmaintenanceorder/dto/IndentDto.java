package cn.crisp.crispmaintenanceorder.dto;

import lombok.Data;
import java.util.List;

@SuppressWarnings({"all"})
@Data
public class IndentDto {
    /**
     * 地址 id
     */
    private Long addressId;

    /**
     * 问题
     */
    private String problem;

    /**
     * 备注
     */
    private String remark;

    /**
     * 图片数组
     */
    private List<String> photos;
}
