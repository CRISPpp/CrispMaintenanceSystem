package cn.crisp.crispmaintenanceuser;

import cn.crisp.entity.User;
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

    private void crispCopyUser(User source, User target) {
        if (target.getIcon() != null) {
            source.setIcon(target.getIcon());
        }

        if (target.getPhone() != null) {
            source.setPhone(target.getPhone());
        }

        if (target.getMail() != null) {
            source.setMail(target.getMail());
        }

        if (target.getUsername() != null) {
            source.setUsername(target.getUsername());
        }

        if (target.getRole() != null) {
            source.setRole(target.getRole());
        }
    }

    @Autowired
    RedissonClient redissonClient;

    @Test
    void testRedisson() {
        User user = new User();
        user.setId(1L);
        user.setPhone("123456");
        User user2 = new User();
        user2.setPhone("123");
        crispCopyUser(user, user2);
        System.out.println(user);
    }

}
