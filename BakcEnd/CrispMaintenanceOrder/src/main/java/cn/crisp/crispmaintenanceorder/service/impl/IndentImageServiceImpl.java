package cn.crisp.crispmaintenanceorder.service.impl;

import cn.crisp.crispmaintenanceorder.mapper.IndentImageMapper;
import cn.crisp.crispmaintenanceorder.mapper.IndentMapper;
import cn.crisp.entity.IndentImage;
import cn.crisp.crispmaintenanceorder.service.IndentImageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@SuppressWarnings({"all"})
@Service
public class IndentImageServiceImpl
        extends ServiceImpl<IndentImageMapper, IndentImage>
        implements IndentImageService
{
    @Autowired
    private IndentImageMapper indentImageMapper;

    /**
     * 根据订单 id 获取订单图片列表
     * @param indentId
     * @return
     */
    @Override
    public List<IndentImage> getByIndentId(Long indentId) {
        LambdaQueryWrapper<IndentImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IndentImage::getIndentId, indentId);
        return indentImageMapper.selectList(wrapper);
    }
}