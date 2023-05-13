package cn.crisp.crispmaintenanceuser.controller;

import cn.crisp.common.Constants;
import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.Executor.CrispExecutor;
import cn.crisp.crispmaintenanceuser.dto.LoginDto;
import cn.crisp.crispmaintenanceuser.entity.ESIndexInfo;
import cn.crisp.crispmaintenanceuser.entity.ESMap;
import cn.crisp.crispmaintenanceuser.entity.User;
import cn.crisp.crispmaintenanceuser.es.ESService;
import cn.crisp.crispmaintenanceuser.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.SneakyThrows;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    ESService esService;

    @SneakyThrows
    @PostMapping("/register")
    public R<String> register(LoginDto loginDto) {
        return userService.register(loginDto);
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
}
