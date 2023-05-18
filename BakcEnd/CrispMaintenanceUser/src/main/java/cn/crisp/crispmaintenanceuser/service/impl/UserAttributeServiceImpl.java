package cn.crisp.crispmaintenanceuser.service.impl;

import cn.crisp.crispmaintenanceuser.entity.UserAttribute;
import cn.crisp.crispmaintenanceuser.mapper.UserAttributeMapper;
import cn.crisp.crispmaintenanceuser.service.UserAttributeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@SuppressWarnings({"all"})
@Service
public class UserAttributeServiceImpl
        extends ServiceImpl<UserAttributeMapper, UserAttribute>
        implements UserAttributeService
{

}