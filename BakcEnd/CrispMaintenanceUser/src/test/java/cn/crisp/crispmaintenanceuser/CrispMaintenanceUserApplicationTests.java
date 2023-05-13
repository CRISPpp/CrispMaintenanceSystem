package cn.crisp.crispmaintenanceuser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class CrispMaintenanceUserApplicationTests {

    @Autowired
    RedissonClient redissonClient;

    @Test
    void testRedisson() {
        System.out.println(redissonClient);
    }

}
