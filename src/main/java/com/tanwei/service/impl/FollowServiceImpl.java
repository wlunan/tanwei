package com.tanwei.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanwei.dto.Result;
import com.tanwei.dto.UserDTO;
import com.tanwei.entity.Follow;
import com.tanwei.entity.User;
import com.tanwei.mapper.FollowMapper;
import com.tanwei.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanwei.service.IUserService;
import com.tanwei.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        // 获取登录用户id
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        // 判断是关注还是取关
        if (isFollow) {
            // 关注，新增数据
            Follow follow = new Follow();
            follow.setFollowUserId(followUserId);
            follow.setUserId(userId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                // 把关注用户id放入redis的set集合 sadd key value
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // 取关，删除数据
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId)
                    .eq("follow_user_id", followUserId)
            );
            if (isSuccess) {
                // 把关注用户id从redis的set集合中移除 srem key value
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        // 获取登陆用户id
        Long userId = UserHolder.getUser().getId();
        // 查询登陆用户是否关注 select * from tb_follow where user_id = ? and follow_user_id = ?
        Integer count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        // 判断是否关注
        return Result.ok(count > 0);
    }

    @Override
    public Result commonFollow(Long queryUserId) {
        // 获取登陆用户id
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        // 求交集，返回两个集合的共同关注用户id
        String key2 = "follows:" + queryUserId;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        // 无交集
        if (intersect == null || intersect.isEmpty()) {
            return Result.ok();
        }
        // 解析id的集合，将一个名为 intersect 的集合中的元素转换为 Long 类型，最终收集成一个 List<Long> 列表
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        // 查询用户
        List<UserDTO> users = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(users);
    }
}
