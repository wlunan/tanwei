package com.tanwei.utils;

public interface ILock {

    /**
     * 获取锁
     * @param timeoutSec 获取锁的超时时间，单位：秒
     * @return true表示获取锁成功，false表示获取锁失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
