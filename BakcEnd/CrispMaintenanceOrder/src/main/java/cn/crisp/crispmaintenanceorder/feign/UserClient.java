package cn.crisp.crispmaintenanceorder.feign;

import cn.crisp.common.R;
import cn.crisp.entity.Address;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@SuppressWarnings({"all"})
@FeignClient("user")
public interface UserClient {
    @GetMapping("/address/{id}")
    R getById(@PathVariable("id") Long id);
}