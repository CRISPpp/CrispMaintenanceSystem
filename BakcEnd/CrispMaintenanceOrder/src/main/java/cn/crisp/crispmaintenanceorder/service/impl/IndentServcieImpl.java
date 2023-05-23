package cn.crisp.crispmaintenanceorder.service.impl;

import cn.crisp.common.Constants;
import cn.crisp.crispmaintenanceorder.dto.IndentDto;
import cn.crisp.crispmaintenanceorder.dto.QueryDto;
import cn.crisp.crispmaintenanceorder.entity.ESMap;
import cn.crisp.crispmaintenanceorder.entity.LoginUser;
import cn.crisp.crispmaintenanceorder.es.ESService;
import cn.crisp.crispmaintenanceorder.feign.UserClient;
import cn.crisp.crispmaintenanceorder.mapper.IndentImageMapper;
import cn.crisp.crispmaintenanceorder.mapper.IndentMapper;
import cn.crisp.crispmaintenanceorder.security.service.TokenService;
import cn.crisp.crispmaintenanceorder.service.IndentImageService;
import cn.crisp.crispmaintenanceorder.vo.PagingVo;
import cn.crisp.entity.Address;
import cn.crisp.entity.Indent;
import cn.crisp.crispmaintenanceorder.service.IndentService;
import cn.crisp.entity.IndentImage;
import cn.crisp.entity.User;
import cn.crisp.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
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
                queryDto.getCurrent(),
                queryDto.getSize()
        );
    }

    /**
     * 故障报修
     * @param indentDto
     * @return
     */
    @Override
    public Indent applyForFault(IndentDto indentDto, HttpServletRequest request) {
        User user = tokenService.getLoginUser(request).getUser();

        //判断是否有处于待支付的订单，如果有，必须先支付后才能申报故障
        LambdaQueryWrapper<Indent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Indent::getUserId, user.getId())
                .eq(Indent::getStatus, Indent.Status.UNPAID);
        if (indentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(0, "您有未支付订单，请支付后再故障报修");
        }

        //获取维修地址
        Address address = (Address) userClient.getById(indentDto.getAddressId()).getData();
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

        return indent;
    }


}