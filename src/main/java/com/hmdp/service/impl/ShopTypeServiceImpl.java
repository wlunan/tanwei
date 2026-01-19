package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.SHOP_TYPE_KEY;
import static com.hmdp.utils.RedisConstants.SHOP_TYPE_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override // lunan实现
    public Result queryShopTypeList() {
        String key = SHOP_TYPE_KEY;
        // 1.查询缓存
        String shopTypeListJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(shopTypeListJson)) {
            // 2.如果存在，直接返回
            List<ShopType> shopTypeList = JSONUtil.toList(shopTypeListJson, ShopType.class);
            return Result.ok(shopTypeList);
        }
        // 3.如果不存在，查询数据库，写入缓存，返回数据
        List<ShopType> shopTypeList =
                query().orderByAsc("sort").list();
        // 4.写入缓存
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopTypeList), SHOP_TYPE_TTL, TimeUnit.MINUTES);
        // 5.返回数据
        return Result.ok(shopTypeList);
    }
}
