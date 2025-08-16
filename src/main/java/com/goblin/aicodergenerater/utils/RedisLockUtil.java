package com.goblin.aicodergenerater.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类 - 基于Redisson实现
 * @Author goblin
 * @Date 2025/1/17
 */
@Component
public class RedisLockUtil {

    private final RedissonClient redissonClient;

    public RedisLockUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 获取分布式锁
     * @param key 锁的key
     * @param waitTime  等待时间
     * @param leaseTime 锁自动过期时间
     * @param timeUnit  锁过期的时间单位
     * @return
     */
    public RLock tryLock(String key, long waitTime,long leaseTime ,TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(key);
        try {
            // 尝试获取锁，最多等待waitTime秒，锁自动过期时间为leaseTime 单位timeUnit
            if (lock.tryLock(waitTime, leaseTime, timeUnit)) {
                return lock;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
    /**
     * 获取分布式锁
     * @param key 锁的key
     * @param timeout 超时时间（秒）
     * @return 锁对象，获取失败返回null
     */
    public RLock tryLock(String key, long timeout) {
        return tryLock(key, timeout,timeout,TimeUnit.SECONDS);
    }

    /**
     * 释放分布式锁
     * @param lock 锁对象
     */
    public void releaseLock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}