package cn.crisp.crispmaintenanceuser.controller;

import cn.crisp.common.Constants;
import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.entity.ESIndexInfo;
import cn.crisp.crispmaintenanceuser.entity.ESMap;
import cn.crisp.crispmaintenanceuser.entity.User;
import cn.crisp.crispmaintenanceuser.es.ESService;
import cn.crisp.crispmaintenanceuser.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.SneakyThrows;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    ESService esService;

    @Autowired
    RedissonClient redissonClient;

    @SneakyThrows
    @GetMapping("/test")
    public R<List<User>> test() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper();
        wrapper.eq(User::getId, 1L);
        List<User> list = new ArrayList<>();
        List<String> id = new ArrayList<>();
        list.add(new User(LocalDateTime.now(), LocalDateTime.now(),1L, 0, 0L, "null", "userONe", "123", "123", "mail"));
        id.add("1");
        list.add(new User(LocalDateTime.now(), LocalDateTime.now(),2L, 0, 0L, "null", "userTwo", "123", "123", "mail"));
        id.add("2");
        list.add(new User(LocalDateTime.now(), LocalDateTime.now(),3L, 0, 0L, "null", "userThree", "123", "123", "mail"));
        id.add("3");
        list.add(new User(LocalDateTime.now(), LocalDateTime.now(),4L, 0, 0L, "null", "userFour", "123", "123", "mail"));
        id.add("4");
        List<ESMap> list1 = new ArrayList<>();
        //list1.add(new ESMap<>("id", 1L));
        return R.success(esService.docGetPage(Constants.USER_ES_INDEX_NAME, User.class, list1, 1, 2));
        //return R.success(esService.docGet(Constants.USER_ES_INDEX_NAME, User.class, list1));
        //return R.success(esService.docDelete("1", Constants.USER_ES_INDEX_NAME));
        //return R.success(esService.docGet("1", Constants.USER_ES_INDEX_NAME, User.class));
        //return R.success(esService.docBatchInsert(list, id, Constants.USER_ES_INDEX_NAME));
    }

    @GetMapping("/hello")
    public void test1() {
        RLock lock = redissonClient.getLock("hello");
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        try {
            System.out.println("加锁" + Thread.currentThread().getId());
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            System.out.println("解锁" + Thread.currentThread().getId());
        }
    }
}
