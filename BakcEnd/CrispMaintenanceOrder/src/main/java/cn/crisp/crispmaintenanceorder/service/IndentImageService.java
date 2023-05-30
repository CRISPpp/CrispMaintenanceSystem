package cn.crisp.crispmaintenanceorder.service;

import cn.crisp.entity.IndentImage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

@SuppressWarnings({"all"})
public interface IndentImageService extends IService<IndentImage> {
    /**
     * 根据订单 id 获取订单图片列表
     * @param indentId
     * @return
     */
    List<IndentImage> getByIndentId(Long indentId);
}