package cn.crisp.crispmaintenanceuser.controller;

import cn.crisp.common.Constants;
import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.entity.ESMap;
import cn.crisp.crispmaintenanceuser.entity.LoginUser;
import cn.crisp.crispmaintenanceuser.es.ESService;
import cn.crisp.crispmaintenanceuser.security.service.SysLoginService;
import cn.crisp.crispmaintenanceuser.service.UserService;
import cn.crisp.crispmaintenanceuser.utils.RedisCache;
import cn.crisp.dto.LoginDto;
import cn.crisp.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.SneakyThrows;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private SysLoginService loginService;

    @Autowired
    ESService esService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisCache redisCache;
    @SneakyThrows
    @PostMapping("/register")
    public R<String> register(@RequestBody LoginDto loginDto) {
        System.out.println(loginDto);
        return userService.register(loginDto);
    }

    @PostMapping("/login")
    public R<String> login(@RequestBody LoginDto loginDto) {
        try {
            //准备返回数据
            String token = loginService.login(loginDto.getPhone(), loginDto.getPassword());
            if (token != null && token.length() != 0) {
                return R.success(token);
            }
            return R.error("登录信息错误");
        }catch (Exception e) {
            return R.error("用户名或密码错误");
        }
    }


    @GetMapping("/getByToken")
    public R<User> getByToken(HttpServletRequest request) {
        return userService.getByToken(request);
    }

    @GetMapping("/{id}")
    public R<User> getById(@PathVariable Long id) {
        User user = userService.selectById(id);
        if (user == null) return R.error("用户不存在");
        return R.success(user);
    }

    @PutMapping()
    public R<User> updateOne(HttpServletRequest request, @RequestBody User user) {
        User ret = userService.updateOne(request, user);
        if (ret == null) {
            return R.error("用户不存在");
        }
        return R.success(ret);
    }


    @PutMapping("/update_phone")
    public R<User> updatePhone(HttpServletRequest request, @RequestBody User user) {
        User ret =  userService.updatePhone(request, user);
        if (ret == null) {
            return R.error("请检查用户信息或者用户权限");
        }
        return R.success(ret);
    }

    @SneakyThrows
    @GetMapping("/test")
    public R<List<User>> test() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper();
        wrapper.eq(User::getId, 1L);
        List<User> list = new ArrayList<>();
        List<String> id = new ArrayList<>();
        list.add(new User(LocalDateTime.now(), LocalDateTime.now(),1L, 0, 0L, "null", "userONe", "123", "123", "mail", 1));
        id.add("1");
        list.add(new User(LocalDateTime.now(), LocalDateTime.now(),2L, 0, 0L, "null", "userTwo", "123", "123", "mail",1));
        id.add("2");
        list.add(new User(LocalDateTime.now(), LocalDateTime.now(),3L, 0, 0L, "null", "userThree", "123", "123", "mail",1));
        id.add("3");
        list.add(new User(LocalDateTime.now(), LocalDateTime.now(),4L, 0, 0L, "null", "userFour", "123", "123", "mail",1));
        id.add("4");
        List<ESMap> list1 = new ArrayList<>();
        //list1.add(new ESMap<>("id", 1L));
        //return R.success(esService.docGetPage(Constants.USER_ES_INDEX_NAME, User.class, list1, 1, 2));
        return R.success(esService.docGet(Constants.USER_ES_INDEX_NAME, User.class, list1));
        //return R.success(esService.docDelete("1", Constants.USER_ES_INDEX_NAME));
        //return R.success(esService.docGet("1", Constants.USER_ES_INDEX_NAME, User.class));
        //return R.success(esService.docBatchInsert(list, id, Constants.USER_ES_INDEX_NAME));
    }

    @GetMapping("/hello")
    public void test1() {
        System.out.println((Object) redisCache.getCacheObject("login_tokens:931f1844-d0f8-4a65-bd3e-0ea6eb1d2902"));

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
