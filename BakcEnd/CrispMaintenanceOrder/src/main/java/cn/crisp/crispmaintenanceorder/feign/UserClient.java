package cn.crisp.crispmaintenanceorder.feign;

import cn.crisp.common.R;
import cn.crisp.dto.PayDto;
import cn.crisp.entity.Address;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings({"all"})
@FeignClient("CrispMaintenanceUser")
public interface UserClient {
    @GetMapping("/user/address/{id}")
    R<Address> getAddressById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @PostMapping("/user/pay")
    R<Boolean> pay(@RequestBody PayDto payDto, @RequestHeader("Authorization") String token);
}