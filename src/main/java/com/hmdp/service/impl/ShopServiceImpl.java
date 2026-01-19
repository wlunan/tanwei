package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.RedisData;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource// 按名称注入
    private StringRedisTemplate stringRedisTemplate;



    /**
     * 原始redis缓存实现
     * @param id
     * @return
     *//*
    @Override
    public Result queryById(Long id) {
        String key = CACHE_SHOP_KEY + id;
        // 1.从redis查询缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断缓存是否命中
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.如果命中，直接返回商品信息
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        // 4.如果未命中，查询数据库
        Shop shop = getById(id);
        // 5.数据库不存在，返回错误
        if (shop == null ) {
            return Result.ok("店铺不存在！");
        }
        // 6.数据库存在，写入缓存
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        // 7.返回商品信息
        return Result.ok(shop);
    }*/

    /**
     * 原始redis缓存实现+解决缓存击穿+缓存雪崩
     * @param id
     * @return
     */
    @Override
    public Result queryById(Long id) {
        // 解决缓存击穿
        Shop shop = queryWithMutex(id);
        return Result.ok(shop);
    }

    /**
     * 解决缓存击穿-互斥锁
     * @param id
     * @return
     */
    public Shop queryWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
        // 1.从redis查询缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断缓存是否命中
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.如果命中，直接返回商品信息
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        // 判断命中的是否是空值（""）
        if (shopJson != null) {
            // 如果 Redis 返回了值，但不是有效 JSON（比如是空字符串），说明这是个‘空缓存’，直接返回 null，别查数据库了。
            return null;
        }

        // 4.如果未命中，查询数据库
        // 4.1 获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2 判断是否获取成功
            if (!isLock) {
                // 4.3 获取失败，休眠并重试
                Thread.sleep(50);
                return queryWithMutex(id); // 递归调用
            }
            // 4.4 获取成功，查询数据库
            shop = getById(id);
            // 模拟重建延时
            Thread.sleep(200);
            // 5.数据库不存在
            if (shop == null ) {
                // 缓存空值，返回错误，防止缓存穿透
                // 给不同的Key的TTL添加随机值，防止缓存雪崩
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL+ RandomUtil.randomLong(1,5), TimeUnit.MINUTES);
                return null;
            }

            // 6.数据库存在，写入缓存
            stringRedisTemplate.opsForValue().set(
                    key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL+RandomUtil.randomLong(1,5), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 释放锁
            unlock(lockKey);
        }
        // 7.返回商品信息
        return shop;

    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1.更新数据库
        updateById(shop);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    public void saveShopToRedis(Long id, Long expireSeconds) throws InterruptedException {
        // 1.查询店铺数据
        Shop shop = getById(id);
        // 2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // 3.写入Redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

}
