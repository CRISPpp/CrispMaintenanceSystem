package cn.crisp.crispmaintenanceuser.service.impl;

import cn.crisp.common.Constants;
import cn.crisp.common.R;
import cn.crisp.crispmaintenanceuser.entity.ESMap;
import cn.crisp.crispmaintenanceuser.es.ESService;
import cn.crisp.crispmaintenanceuser.security.service.TokenService;
import cn.crisp.crispmaintenanceuser.utils.RedisCache;
import cn.crisp.entity.Address;
import cn.crisp.crispmaintenanceuser.mapper.AddressMapper;
import cn.crisp.crispmaintenanceuser.service.AddressService;
import cn.crisp.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"all"})
@Transactional
@Service
public class AddressServiceImpl
        extends ServiceImpl<AddressMapper, Address>
        implements AddressService
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private ESService esService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public R<List<Address>> getAddress(HttpServletRequest request) {
        User user = tokenService.getLoginUser(request).getUser();
        if (user == null) return R.error("登录信息错误");

        List<Address> ret = null;
        ESMap<Long> esMap = new ESMap("userId", user.getId());
        List<ESMap> list = new ArrayList<>();
        list.add(esMap);

        ret = esService.docGet(Constants.ADDRESS_ES_INDEX_NAME, Address.class, list);


        return R.success(ret);
    }

    @Override
    public R<String> addAddress(HttpServletRequest request, Address address) {
        User user = tokenService.getLoginUser(request).getUser();
        if (user == null) return R.error("登录信息错误");

        List<Address> list = this.getAddress(request).getData();

        if (list.size() >= 10) {
            return R.error("地址数量不能超过10");
        }


        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.ADDRESS_LOCK_NAME + user.getId());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        try {
            address.setUserId(user.getId());
            this.save(address);

            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, user.getId());

            List<Address> addresses = this.list(wrapper);

            List<String> rets = new ArrayList<>();

            addresses.stream().forEach(address1 -> {
                rets.add(esService.docInsert(address1, address1.getId().toString(), Constants.ADDRESS_ES_INDEX_NAME));
            });


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }
        return R.success("添加成功");
    }

    @Override
    public R<String> delAddress(HttpServletRequest request, Long id) {
        User user = tokenService.getLoginUser(request).getUser();
        Address address = esService.docGet(id.toString(), Constants.ADDRESS_ES_INDEX_NAME, Address.class);
        if (address == null) return R.error("该地址不存在");
        if (!user.getId().equals(address.getUserId())) return R.error("无法删除其他用户的地址");

        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.ADDRESS_LOCK_NAME + user.getId());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        try {
            boolean ret = this.removeById(id);
            if (!ret) return R.error("地址不存在");
            String ret1 = esService.docDelete(id.toString(), Constants.ADDRESS_ES_INDEX_NAME);
            if (!ret1.equals("DELETED")) return R.error("es删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }

        return R.success("删除成功");
    }

    @Override
    public R<String> updateDefault(HttpServletRequest request, Long id) {
        User user = tokenService.getLoginUser(request).getUser();
        Address address = esService.docGet(id.toString(), Constants.ADDRESS_ES_INDEX_NAME, Address.class);
        if (address == null) return R.error("该地址不存在");
        if (!user.getId().equals(address.getUserId())) return R.error("无法使用其他用户的地址");

        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.ADDRESS_LOCK_NAME + user.getId());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        try {
            LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Address::getUserId, user.getId());
            wrapper.eq(Address::getIsDefault, 1);
            Address addressOld = addressMapper.selectOne(wrapper);
            List<String> rets = new ArrayList<>();
            if (addressOld != null) {
                addressOld.setIsDefault(0);
                rets.add(esService.docInsert(addressOld, addressOld.getId().toString(), Constants.ADDRESS_ES_INDEX_NAME));
                addressOld.setUpdateTime(null);
                boolean ret = this.updateById(addressOld);
                if (!ret) return R.error("更新失败");
            }

            address.setIsDefault(1);
            esService.docInsert(address, address.getId().toString(), Constants.ADDRESS_ES_INDEX_NAME);
            address.setUpdateTime(null);
            boolean ret1 = this.updateById(address);
            if (!ret1) return R.error("更改失败");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }

        return R.success("修改成功");
    }

    @Override
    public R<Address> updateOne(HttpServletRequest request, Address address) {
        User user = tokenService.getLoginUser(request).getUser();
        if (address == null) return R.error("该地址不存在");
        if (address.getIsDefault() != null) return R.error("不允许修改默认地址");
        if (!user.getId().equals(address.getUserId())) return R.error("无法使用其他用户的地址");

        //将数据库的修改和es的转为原子操作
        RLock lock = redissonClient.getLock(Constants.ADDRESS_LOCK_NAME + user.getId());
        //阻塞式等待，默认为30s过期时间，业务过长会自动续期，加锁业务执行完（通过线程判断）不会自动续期，30s后过期
        lock.lock();
        try {
            Boolean ret = this.updateById(address);
            if (!ret) return R.error("更新失败");
            address = this.getById(address.getId());
            esService.docInsert(address, address.getId().toString(), Constants.ADDRESS_ES_INDEX_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            lock.unlock();
        }

        return R.success(address);
    }
}