package cn.crisp.crispmaintenanceuser.service.impl;

import cn.crisp.crispmaintenanceuser.entity.Address;
import cn.crisp.crispmaintenanceuser.mapper.AddressMapper;
import cn.crisp.crispmaintenanceuser.service.AddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@SuppressWarnings({"all"})
@Service
public class AddressServiceImpl
        extends ServiceImpl<AddressMapper, Address>
        implements AddressService
{

}