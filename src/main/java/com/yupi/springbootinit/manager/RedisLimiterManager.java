package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * ClassName: RedisLimiterManager
 * Package: com.yupi.springbootinit.manager
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/24 21:40
 * @Version 1.0
 */
@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;
    //key区分不同的限流器
    public void doRateLimit(String key){
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL,2L,1L, RateIntervalUnit.SECONDS);
        boolean canOp = rateLimiter.tryAcquire(1);
        if(!canOp){
            throw  new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }

    }
}
