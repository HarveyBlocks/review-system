package com.harvey.review_system.utils;


/**
 * 缓存有关常量类,测试用,有关时间的.统一单位秒
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-01-05 16:53
 */
public class ShopCacheConstants implements CacheConstants {

    @Override
    public Long cacheNullTtl(){return 3L;}
    @Override
    public Long cacheTtl() {return 3L;}
    @Override
    public String cacheKey() {return  "cache:shop:";}
    @Override
    public String hotKey() {return  "hot:shop:";}
    @Override
    public Long hotExpire() {return 6L;}
    @Override
    public String lockKey() {return "lock:shop:";}
    @Override
    public Long lockTtl() {return  6L;}
}
