package cn.crisp.crispmaintenanceorder.controller;

import cn.crisp.common.R;
import cn.crisp.crispmaintenanceorder.dto.IndentDto;
import cn.crisp.crispmaintenanceorder.dto.QueryDto;
import cn.crisp.crispmaintenanceorder.security.service.TokenService;
import cn.crisp.crispmaintenanceorder.service.IndentService;
import cn.crisp.crispmaintenanceorder.utils.ParamUtils;
import cn.crisp.crispmaintenanceorder.utils.RedisCache;
import cn.crisp.entity.Indent;
import cn.crisp.entity.User;
import cn.crisp.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("all")
@RestController
@RequestMapping("/indent")
public class IndentController {
    @Autowired
    TokenService tokenService;

    @Autowired
    RedisCache redisCache;

    @Autowired
    private IndentService indentService;

    @GetMapping("/test")
    public R<User> test(HttpServletRequest request) {
        System.out.println((Object) redisCache.getCacheObject("login_tokens:931f1844-d0f8-4a65-bd3e-0ea6eb1d2902"));
        return R.success(tokenService.getLoginUser(request).getUser());
    }

    /**
     * 获取订单列表
     * @param queryDto 查询条件
     * @return
     */
    @PostMapping("/list")
    public R list(@RequestBody QueryDto<Indent> queryDto) {
        //分页
        if (queryDto.getPaging()) {
            return R.success(indentService.listPage(queryDto));
        }
        //不分页
        else {
            return R.success(indentService.list(queryDto.getConditon()));
        }
    }

    /**
     * 故障申报
     * @param indentDto
     * @param request
     * @return
     */
    @PutMapping
    public R applyForFault(@RequestBody IndentDto indentDto, HttpServletRequest request) {
        ParamUtils.checkFieldNotNull(
                indentDto,
                IndentDto::getAddressId,
                IndentDto::getProblem,
                IndentDto::getRemark,
                IndentDto::getPhotos
        );

        //只有普通用户才能故障申报
        if (!tokenService.getLoginUser(request).getUser().getRole().equals(User.Role.COMMON_USER)) {
            throw new BusinessException(0, "普通用户才能故障申报");
        }

        //检测图片数目
        if (indentDto.getPhotos().size() < 1 || indentDto.getPhotos().size() > 3) {
            throw new BusinessException(0, "图片至少1张，最多3张");
        }

        return R.success(indentService.applyForFault(indentDto, request));
    }
}
