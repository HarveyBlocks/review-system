package com.harvey.review_system.utils;

/**
 * CacheClient需要的常量类,有关时间的.统一单位秒
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-01-05 16:44
 */
public interface CacheConstants {
    default Long cacheNullTtl(){return 2*60L;}
    default  Long cacheTtl() {return 30*60L;}
    default  String cacheKey() {return  "cache:object:";}
    default  String hotKey() {return  "hot:object:";}
    default  Long hotExpire() {return 60*60*2L;}
    default  String hotDataField() {return "data";}
    default  String hotExpireField() {return "expire";}
    default  String lockKey() {return "lock:object:";}
    default  Long lockTtl() {return  6*60L;}
}
