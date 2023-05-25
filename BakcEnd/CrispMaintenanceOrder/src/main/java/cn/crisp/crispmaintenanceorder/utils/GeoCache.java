package cn.crisp.crispmaintenanceorder.utils;

import cn.crisp.common.Constants;
import cn.crisp.crispmaintenanceorder.vo.PagingVo;
import cn.crisp.exception.BusinessException;
import org.redisson.api.GeoUnit;
import org.redisson.api.RGeo;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.api.geo.GeoSearchArgs;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"all"})
@Component
public class GeoCache {
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 维修工程师放入 GEO 时的 field
     * @param engineerId
     * @return
     */
    private String engineerField(Long engineerId) {
        return (Constants.GEO_USER_FIELD_PREFIX + engineerId).intern();
    }

    /**
     * 根据放入 GEO 的 field 返回维修工程师的 id
     * @param engineerField
     * @return
     */
    private Long engineerId(String engineerField) {
        return Long.parseLong(engineerField.substring(Constants.GEO_USER_FIELD_PREFIX.length()));
    }

    /**
     * 订单放入 GEO 时的 field
     * @param indentId
     * @return
     */
    private String indentField(Long indentId) {
        return (Constants.GEO_INDENT_FIELD_PREFIX + indentId).intern();
    }

    /**
     * 根据放入 GEO 的 field 返回维修工程师的 id
     * @param engineerField
     * @return
     */
    private Long indentId(String indentField) {
        return Long.parseLong(indentField.substring(Constants.GEO_INDENT_FIELD_PREFIX.length()));
    }

    /**
     * 将新增的订单位置信息放入 GEO
     * @param longitude 经度
     * @param latitude 纬度
     * @param id 订单 id
     */
    public void add(double longitude, double latitude, Long id) {
        RGeo<String> geo = redissonClient.getGeo(Constants.GEO_NAME, StringCodec.INSTANCE);
        geo.add(longitude, latitude, indentField(id));
    }

    /**
     * 维修工程师接单后将 id 删除
     * @param id
     */
    public void remove(Long id) {
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(
                Constants.GEO_NAME,
                StringCodec.INSTANCE
        );
        zset.remove(indentField(id));
    }

    /**
     * 返回距离 (longitude, latitude) dist 公里内的订单 id
     * @param longitude 经度
     * @param latitude 纬度
     * @param dist 距离
     * @return
     */
    public List<Long> list(double longitude, double latitude, double dist) {
        RGeo<String> geo = redissonClient.getGeo(Constants.GEO_NAME, StringCodec.INSTANCE);
        return geo.radius(longitude, latitude, dist, GeoUnit.KILOMETERS)
                .stream()
                .map(str -> indentId(str))
                .collect(Collectors.toList());
    }

    /**
     * 返回距离 (longitude, latitude) dist 公里内的订单 id
     * @param longitude 经度
     * @param latitude 纬度
     * @param dist 距离
     * @param size 一页的大小
     * @param current 第几页，从 0 开始
     * @return
     */
    public PagingVo<Long> listPage(
            double longitude,
            double latitude,
            double dist,
            int size,
            int current
    ) {
        //页大小必须大于0，页数必须大于等于0
        if (size <= 0 || current < 0) {
            throw new BusinessException(0, "页大小必须大于0，页数必须大于等于0");
        }

        RGeo<String> geo = redissonClient.getGeo(Constants.GEO_NAME, StringCodec.INSTANCE);
        List<Long> totalList = geo.radius(longitude, latitude, dist, GeoUnit.KILOMETERS)
                .stream()
                .map(str -> indentId(str))
                .collect(Collectors.toList());

        return new PagingVo<Long>(
                totalList.size(),
                totalList.stream().skip(current * size).limit(size).collect(Collectors.toList())
        );
    }

    /**
     * 获取订单到维修工程师的距离，单位 km
     * @param longitude 维修工程师的经度
     * @param latitude 维修工程师的纬度
     * @param engineerId 维修工程师 id
     * @param indentId 订单 id
     * @return
     */
    public double dist(double longitude, double latitude, Long engineerId, Long indentId) {
        RGeo<String> geo = redissonClient.getGeo(Constants.GEO_NAME, StringCodec.INSTANCE);
        geo.add(longitude, latitude, engineerField(engineerId));
        return geo.dist(engineerField(engineerId), indentField(indentId), GeoUnit.KILOMETERS);
    }
}