package cn.crisp.crispmaintenanceorder.service.impl;

import cn.crisp.crispmaintenanceorder.mapper.IndentMapper;
import cn.crisp.crispmaintenanceorder.model.entity.Indent;
import cn.crisp.crispmaintenanceorder.service.IndentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@SuppressWarnings({"all"})
@Service
public class IndentServcieImpl
        extends ServiceImpl<IndentMapper, Indent>
        implements IndentService
{

}