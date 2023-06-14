package cn.crisp.crispmaintenanceorder.controller;

import cn.crisp.common.R;
import cn.crisp.crispmaintenanceorder.dto.*;
import cn.crisp.crispmaintenanceorder.security.service.TokenService;
import cn.crisp.crispmaintenanceorder.service.IndentService;
import cn.crisp.crispmaintenanceorder.utils.ParamUtils;
import cn.crisp.crispmaintenanceorder.utils.RedisCache;
import cn.crisp.dto.PayDto;
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
            return R.success(indentService.list(queryDto.getCondition()));
        }
    }

    @PostMapping("/list_unprocessed")
    public R listUnprocessed(@RequestBody DispatchDto dispatchDto, HttpServletRequest request) {
        ParamUtils.checkFieldNotNull(
                dispatchDto,
                DispatchDto::getLongitude,
                DispatchDto::getLatitude,
                DispatchDto::getDist
        );

        if (dispatchDto.getLongitude() < -180 || dispatchDto.getLongitude() > 180) {
            throw new BusinessException(0, "经度" + dispatchDto.getLongitude() + "不合法");
        }
        if (dispatchDto.getLatitude() < -90 || dispatchDto.getLatitude() > 90) {
            throw new BusinessException(0, "纬度" + dispatchDto.getLatitude() + "不合法");
        }
        if (dispatchDto.getDist() <= 0) {
            throw new BusinessException(0, "距离必须为正数");
        }

        //只有维修工程师才可以访问该接口
        if (!tokenService.getLoginUser(request).getUser().getRole().equals(User.Role.ENGINEER)) {
            throw new BusinessException(0, "只有维修工程师才可以访问该接口");
        }

        return R.success(indentService.listUnprocessed(dispatchDto));
    }

    /**
     * 根据 id 获取订单信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R getById(@PathVariable("id") Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(0, "id不能为null");
        }
        Indent indent = indentService.getById(id, request);
        if (indent == null) {
            throw new BusinessException(0, "订单不存在");
        }
        return R.success(indent);
    }

    /**
     * 故障申报
     * @param indentDto
     * @param request
     * @return
     */
    @PostMapping
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

    /**
     * 接单
     * @param receiveDto
     * @param request
     * @return
     */
    @PutMapping("/receive_indent")
    public R receiveIndent(@RequestBody ReceiveDto receiveDto, HttpServletRequest request) {
        ParamUtils.checkFieldNotNull(
                receiveDto,
                ReceiveDto::getIndentId,
                ReceiveDto::getLongitude,
                ReceiveDto::getLatitude
        );
        return R.success(indentService.receiveIndent(request, receiveDto));
    }

    /**
     * 确认维修成功
     * @param repairDto
     * @param request
     * @return
     */
    @PutMapping("/repair")
    public R repair(@RequestBody RepairDto repairDto, HttpServletRequest request) {
        ParamUtils.checkFieldNotNull(
                repairDto,
                RepairDto::getIndentId,
                RepairDto::getPhotos
        );
        if (repairDto.getPhotos().size() < 1 || repairDto.getPhotos().size() > 3) {
            throw new BusinessException(0, "维修成功的图片至少 1 张，最多 3 张");
        }
        return R.success(indentService.repair(request, repairDto));
    }

    /**
     * 支付
     * @param payDto
     * @param request
     * @return
     */
    @PutMapping("/pay/{indentId}")
    public R pay(@PathVariable("indentId") Long indentId, HttpServletRequest request) {
        return R.success(indentService.pay(request, indentId));
    }

    /**
     * 评价订单
     * @param evaluateDto
     * @param request
     * @return
     */
    @PutMapping("/evaluate")
    public R evaluate(@RequestBody EvaluateDto evaluateDto, HttpServletRequest request) {
        ParamUtils.checkFieldNotNull(
                evaluateDto,
                EvaluateDto::getIndentId,
                EvaluateDto::getQuality
        );
        double v = evaluateDto.getQuality().doubleValue();
        if (v < 0.0 || v > 5.0) {
            throw new BusinessException(0, "评分范围是 0.0 ~ 5.0");
        }
        return R.success(indentService.evaluate(request, evaluateDto));
    }
}
