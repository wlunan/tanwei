package com.hmdp.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Redis中存储的数据对象
 */
@Data
public class RedisData {

    private LocalDateTime expireTime;
    private Object data;
}
