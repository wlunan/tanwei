package com.hmdp;

import com.hmdp.config.RedisConfig;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class HmdpTest {
    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private RedissonClient redissonClient;
    private static final ExecutorService es = java.util.concurrent.Executors.newFixedThreadPool(10);
    @Test
    void test() {
        shopService.saveShopToRedis(1L, 10L);
    }

    @Test
    void testIdWorker() throws InterruptedException {
        // 开启多线程，生成ID
        CountDownLatch latch = new CountDownLatch(300);
        // 10个线程，每个线程生成100个ID
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }

    @Test
    void testRedisson() throws InterruptedException {
        // 获取锁
        RLock lock = redissonClient.getLock("anyLock");
        // 尝试获取锁
        boolean isLock = lock.tryLock(1, 10, TimeUnit.SECONDS);
        //判断获取锁成功
        if(isLock){
            try{
                System.out.println("执行业务");
            }finally{
                //释放锁
                lock.unlock();
            }

        }
        }
}
