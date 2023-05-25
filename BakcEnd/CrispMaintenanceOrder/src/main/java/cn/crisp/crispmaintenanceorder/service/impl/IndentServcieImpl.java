package cn.crisp.crispmaintenanceorder.service.impl;

import cn.crisp.common.Constants;
import cn.crisp.common.R;
import cn.crisp.crispmaintenanceorder.dto.*;
import cn.crisp.crispmaintenanceorder.entity.ESMap;
import cn.crisp.crispmaintenanceorder.entity.LoginUser;
import cn.crisp.crispmaintenanceorder.es.ESService;
import cn.crisp.crispmaintenanceorder.feign.UserClient;
import cn.crisp.crispmaintenanceorder.mapper.IndentMapper;
import cn.crisp.crispmaintenanceorder.security.service.TokenService;
import cn.crisp.crispmaintenanceorder.service.IndentImageService;
import cn.crisp.crispmaintenanceorder.utils.GeoCache;
import cn.crisp.crispmaintenanceorder.vo.PagingVo;
import cn.crisp.entity.Address;
import cn.crisp.entity.Indent;
import cn.crisp.crispmaintenanceorder.service.IndentService;
import cn.crisp.entity.IndentImage;
import cn.crisp.entity.User;
import cn.crisp.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"all"})
@Transactional
@Service
public class IndentServcieImpl
        extends ServiceImpl<IndentMapper, Indent>
        implements IndentService
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private ESService esService;

    @Autowired
    private IndentMapper indentMapper;

    @Autowired
    private IndentImageService indentImageService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private GeoCache geoCache;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取 Indent 对象的 ES 查询条件
     * @param indent
     * @return
     */
    private List<ESMap> getESMap(Indent indent) {
        List<ESMap> list = new ArrayList<>();
        if (indent != null) {
            try {
                for (Field field : Indent.class.getDeclaredFields()) {
                    Object v = null;
                    if ((v = field.get(indent)) != null) {
                        list.add(new ESMap(field.getName(), v));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 获取订单列表
     * @param condition 查询条件
     * @return
     */
    @Override
    public List<Indent> list(Indent condition) {
        return esService.docGet(
                Constants.INDENT_ES_INDEX_NAME,
                Indent.class,
                getESMap(condition)
        );
    }

    /**
     * 分页获取订单列表
     * @param queryDto
     * @return
     */
    @Override
    public PagingVo<Indent> listPage(QueryDto<Indent> queryDto) {
        return esService.docGetPage(
                Constants.INDENT_ES_INDEX_NAME,
                Indent.class,
                getESMap(queryDto.getConditon()),
                queryDto.getSkip(),
                queryDto.getSize()
        );
    }

    /**
     * 根据维修工程师的位置获取待处理列表，从近到远
     * @param dispatchDto
     * @return
     */
    @Override
    public PagingVo<Indent> listUnprocessed(DispatchDto dispatchDto) {
        PagingVo<Long> pagingVo = geoCache.listPage(
                dispatchDto.getLongitude(),
                dispatchDto.getLatitude(),
                dispatchDto.getDist(),
                dispatchDto.getSize(),
                dispatchDto.getCurrent()
        );
        PagingVo<Indent> result = new PagingVo<Indent>();
        //从 es 中查找，减轻数据库的压力
        result.setRecords(
                pagingVo.getRecords().stream()
                        .map(id -> esService.docGet(id.toString(), Constants.INDENT_ES_INDEX_NAME, Indent.class))
                        .collect(Collectors.toList())
        );
        result.setTotal(pagingVo.getTotal());
        return result;
    }

    /**
     * 根据 id 获取订单信息
     * @param id
     * @return
     */
    @Override
    public Indent getById(Long id) {
        return esService.docGet(id.toString(), Constants.INDENT_ES_INDEX_NAME, Indent.class);
    }

    /**
     * 故障报修
     * @param indentDto
     * @return
     */
    @Override
    public Indent applyForFault(IndentDto indentDto, HttpServletRequest request) {
        LoginUser loginUser = tokenService.getLoginUser(request);
        User user = loginUser.getUser();

        //判断是否有处于待支付的订单，如果有，必须先支付后才能申报故障
        LambdaQueryWrapper<Indent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Indent::getUserId, user.getId())
                .eq(Indent::getStatus, Indent.Status.UNPAID);
        if (indentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(0, "您有未支付订单，请支付后再故障报修");
        }

        //获取维修地址
        R<Address> r = userClient.getAddressById(indentDto.getAddressId(), request.getHeader("Authorization"));
        Address address = r.getData();
        if (address == null) {
            throw new BusinessException(0, "地址不存在");
        }

        //插入订单
        Indent indent = new Indent();
        indent.setUserId(address.getUserId());
        indent.setName(address.getName());
        indent.setSex(address.getSex());
        indent.setPhone(address.getPhone());
        indent.setAddressDetail(address.getDetail());
        indent.setLatitude(address.getLatitude());
        indent.setLongitude(address.getLongitude());
        indent.setProblem(indentDto.getProblem());
        indent.setRemark(indentDto.getRemark());
        indent.setStatus(Indent.Status.UNPROCESSED);
        indentMapper.insert(indent);

        //插入订单图片
        List<String> photos = indentDto.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            List<IndentImage> list = photos.stream().map(icon -> {
                IndentImage image = new IndentImage();
                image.setIndentId(indent.getId());
                image.setIcon(icon);
                image.setType(IndentImage.Type.PRE_PROCESS);
                return image;
            }).collect(Collectors.toList());
            indentImageService.saveBatch(list);
        }

        //同步到 ES，再查询一遍是为了获取 createTime、updateTime 等信息
        Indent order = indentMapper.selectById(indent.getId());
        esService.docInsert(order, order.getId().toString(), Constants.INDENT_ES_INDEX_NAME);

        //加入 GEO
        geoCache.add(order.getLongitude(), order.getLatitude(), order.getId());

        return indent;
    }

    /**
     * 接单
     * @param request
     * @param receiveDto 接单所需要的参数
     * @return
     */
    @Override
    public boolean receiveIndent(HttpServletRequest request, ReceiveDto receiveDto) {
        User user = tokenService.getLoginUser(request).getUser();
        //非维修工程师不能接单
        if (!user.getRole().equals(User.Role.ENGINEER)) {
            throw new BusinessException(0, "非维修工程师不能接单");
        }
        //维修师傅一个时刻最多只能有 3 个维修中的订单，如果多于 3 个，不允许接单
        LambdaQueryWrapper<Indent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Indent::getStatus, Indent.Status.PROCESSING)
                .eq(Indent::getEngineerId, user.getId());
        if (indentMapper.selectCount(wrapper) > 3) {
            throw new BusinessException(0, "最多只能同时接3个订单");
        }

        Indent indent = indentMapper.selectById(receiveDto.getIndentId());
        if (indent == null) {
            throw new BusinessException(0, "订单不存在");
        }
        if (!indent.getStatus().equals(Indent.Status.UNPROCESSED)) {
            throw new BusinessException(0, "该订单已被接单");
        }
        indent.setEngineerId(user.getId());
        indent.setStatus(Indent.Status.PROCESSING);
        //计算上门费，假设距离 dist 公里，则费用为 8 + dist * 2
        double dist = geoCache.dist(
                receiveDto.getLongitude(),
                receiveDto.getLatitude(),
                user.getId(),
                indent.getId()
        );
        indent.setCost(new BigDecimal(8 + dist * 2));

        RLock lock = redissonClient.getLock(Constants.INDENT_LOCK_NAME + indent.getId());
        try {
            lock.lock();
            if (indentMapper.updateById(indent) <= 0) {
                throw new BusinessException(0, "接单失败");
            }
            //接单成功，更新 es
            esService.docInsert(indent, indent.getId().toString(), Constants.INDENT_ES_INDEX_NAME);
            //将订单从 GEO 中删除
            geoCache.remove(indent.getId());
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 确认维修成功
     * @param request
     * @param repairDto
     * @return
     */
    @Override
    public boolean repair(HttpServletRequest request, RepairDto repairDto) {
        User user = tokenService.getLoginUser(request).getUser();
        if (!user.getRole().equals(User.Role.ENGINEER)) {
            throw new BusinessException(0, "非维修工程师不能确认维修");
        }

        Indent indent = indentMapper.selectById(repairDto.getIndentId());
        if (indent == null) {
            throw new BusinessException(0, "订单不存在");
        }
        if (!user.getId().equals(indent.getEngineerId())) {
            throw new BusinessException(0, "该订单不属于该维修工程师");
        }
        if (!indent.getStatus().equals(Indent.Status.PROCESSING)) {
            throw new BusinessException(0, "该订单不处于处理中的状态");
        }

        RLock lock = redissonClient.getLock(Constants.INDENT_LOCK_NAME + indent.getId());
        try {
            lock.lock();
            //修改订单状态
            indent.setStatus(Indent.Status.UNPAID);
            if (indentMapper.updateById(indent) <= 0) {
                throw new BusinessException(0, "确认失败");
            }
            //插入图片
            indentImageService.saveBatch(
                repairDto.getPhotos().stream()
                        .map(icon -> {
                            IndentImage image = new IndentImage();
                            image.setIndentId(indent.getId());
                            image.setIcon(icon);
                            image.setType(IndentImage.Type.POST_PROCESS);
                            return image;
                        }).collect(Collectors.toList())
            );
            //更新到 es
            esService.docInsert(indent, indent.getId().toString(), Constants.INDENT_ES_INDEX_NAME);
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 评价
     * @param request
     * @param evaluateDto
     * @return
     */
    @Override
    public boolean evaluate(HttpServletRequest request, EvaluateDto evaluateDto) {
        User user = tokenService.getLoginUser(request).getUser();
        if (!user.getRole().equals(User.Role.COMMON_USER)) {
            throw new BusinessException(0, "只有普通用户才能评价订单");
        }

        Indent indent = indentMapper.selectById(evaluateDto.getIndentId());
        if (indent == null) {
            throw new BusinessException(0, "订单不存在");
        }
        if (!user.getId().equals(indent.getUserId())) {
            throw new BusinessException(0, "不能评价其他用户的订单");
        }
        if (!indent.getStatus().equals(Indent.Status.UNEVALUATED)) {
            throw new BusinessException(0, "该订单不处于待评价的状态");
        }

        RLock lock = redissonClient.getLock(Constants.INDENT_LOCK_NAME + indent.getId());
        try {
            lock.lock();
            indent.setStatus(Indent.Status.COMPLETED);
            indent.setQuality(evaluateDto.getQuality());
            if (indentMapper.updateById(indent) <= 0) {
                throw new BusinessException(0, "评价失败");
            }

            //评价成功，更新维修工程师的 quality
            //计算方式是 全部工单的评价 quality / 总工单数目
            QueryWrapper<Indent> wrapper = new QueryWrapper<>();
            wrapper.select("avg(quality) as quality_avg")
                    .eq("engineer_id", indent.getEngineerId());
            Double qualityAvg = (Double) indentMapper.selectMaps(wrapper).get(0).get("quality_avg");
            //TODO 调用 user 模块，更新维修工程师的 quality

            //更新 es
            esService.docInsert(indent, indent.getId().toString(), Constants.INDENT_ES_INDEX_NAME);
        } finally {
            lock.unlock();
        }
        return true;
    }
}