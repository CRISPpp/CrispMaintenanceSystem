package cn.crisp.crispmaintenanceuser.service;

import cn.crisp.common.R;
import cn.crisp.entity.Address;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@SuppressWarnings({"all"})
public interface AddressService extends IService<Address> {
    R<List<Address>> getAddress(HttpServletRequest request);
    R<String> addAddress(HttpServletRequest request, Address address);
    R<String> delAddress(HttpServletRequest request, Long id);
    R<String> updateDefault(HttpServletRequest request, Long id);
}