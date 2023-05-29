package cn.crisp.crispmaintenanceorder;

import cn.crisp.common.Constants;
import cn.crisp.crispmaintenanceorder.es.ESService;
import cn.crisp.crispmaintenanceorder.utils.GeoCache;
import org.junit.jupiter.api.Test;
import org.redisson.api.RGeo;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CrispMaintenanceOrderApplicationTests {
    @Autowired
    private ESService esService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GeoCache geoCache;

    @Test
    void contextLoads() {
//        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(
//                Constants.GEO_NAME,
//                StringCodec.INSTANCE
//        );
//        zset.remove("1661647791465127937");
//        zset.remove("1661647370679967746");
//        zset.remove("1661647981597122561");

        geoCache.add(35, 40, 1661647370679967746l);
        geoCache.add(5, 10, 1661647791465127937l);
        geoCache.add(135, 20, 1661647981597122561l);

        StringBuilder builder = null;
    }

}
