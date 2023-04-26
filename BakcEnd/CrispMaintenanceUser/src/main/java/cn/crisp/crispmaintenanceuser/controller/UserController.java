package cn.crisp.crispmaintenanceuser.controller;

import cn.crisp.common.Constants;
import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.entity.ESIndexInfo;
import cn.crisp.crispmaintenanceuser.entity.User;
import cn.crisp.crispmaintenanceuser.es.ESService;
import cn.crisp.crispmaintenanceuser.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.SneakyThrows;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
    @GetMapping("/test")
    public R<String> test() {
        List<User> list = new ArrayList<>();
        List<String> id = new ArrayList<>();
        list.add(new User(LocalDateTime.now(), LocalDateTime.now(),1L, 0, 0L, "null", "userONe", "123", "123", "mail"));
        id.add("1");
        list.add(new User(LocalDateTime.now(), LocalDateTime.now(),2L, 0, 0L, "null", "userTwo", "123", "123", "mail"));
        id.add("2");


        //return R.success(esService.docDelete("1", Constants.USER_ES_INDEX_NAME));
        //return R.success(esService.docGet("1", Constants.USER_ES_INDEX_NAME, User.class));
        return R.success(esService.docBatchInsert(list, id, Constants.USER_ES_INDEX_NAME));
    }
}
