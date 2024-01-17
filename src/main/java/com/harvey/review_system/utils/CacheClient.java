package com.harvey.review_system.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.harvey.review_system.entity.Entity;
import org.apache.ibatis.logging.Log;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 有关时间的.统一单位秒
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * +
 * @date 2024-01-05 16:02
 */
public class CacheClient<T extends Entity> {
    private final CacheConstants cacheConstants;
    private final Class<T> entityType;
    private final String entityName;
    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate, Class<T> entityType, Log log, CacheConstants cacheConstants) {
        this.entityType = entityType;
        this.entityName = entityType.getSimpleName();
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheConstants = cacheConstants;
        this.logger = log;
    }

    public CacheClient(StringRedisTemplate stringRedisTemplate, Class<T> entityType, Log log) {
        this(stringRedisTemplate, entityType, log, new CacheConstants() {
        });
    }

    private final Log logger;

    /**
     * 依据ID查找用户, 解决穿透
     *
     * @param id      id
     * @param getById 方法
     * @return 查询到的结果
     */
    public T queryById(Long id, Function<Long, T> getById) {
        logger.debug("queryById");
        T t;
        String key = cacheConstants.cacheKey() + id;
        // 从缓存查
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            // 缓存存在
            logger.debug("缓存" + entityName + ":" + id + "存在");
            if (json.isEmpty()) {
                logger.error("查询的" + entityName + ":" + id + "是假数据");
                // 我们的假数据
                return null;
            }
            logger.debug("查询的" + entityName + ":" + id + "不是假数据");
            t = JSONUtil.toBean(json, entityType);
            return t;
        }
        // 缓存不存在
        logger.debug("缓存" + entityName + ":" + id + "不存在");
        return getFromDbAndWriteToCache(id, key, getById);
    }

    /**
     * 依靠互斥锁实现防止,击穿,同时还防止了雪崩
     *
     * @param id      id
     * @param getById 函数
     * @return entity
     */
    public T queryMutexFixByLock(Long id, Function<Long, T> getById) {
        logger.debug("queryMutexFixByLock");
        T t = null;
        String key = cacheConstants.cacheKey() + id;
        while (true) {
            // 从缓存查
            logger.debug(entityName + ":" + id + "从缓存查");
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json != null) {
                // 缓存存在
                logger.debug(entityName + ":" + id + "缓存存在");
                if (json.isEmpty()) {
                    // 我们的假数据, 为了应对穿透
                    logger.error(entityName + ":" + id + "是之前准备的假数据");
                    return null;
                }
                logger.debug(entityName + ":" + id + "不是假数据");
                t = JSONUtil.toBean(json, entityType);
                return t;
            }
            logger.debug("缓存不存在" + entityName + ":" + id);
            String lockKey = cacheConstants.lockKey() + id;
            try {

                if (lock(lockKey)) {// 每个店铺要有自己的锁
                    logger.debug("进入锁");
                    t = getFromDbAndWriteToCache(id, key, getById);
                    // 完成读取要释放锁
                    unlock(lockKey);
                    logger.debug("完成从数据库读取" + entityName + ":" + id + "并写入缓存");
                    return t;
                } else {
                    //
                    logger.debug("等待中...");
                    Thread.sleep(100);
                    // 没出现问题, 不是做读写操作的, 不需要释放锁
                }
            } catch (Exception e) {
                // 发生问题要释放锁
                unlock(lockKey);
                logger.error("发生问题了, 但依旧释放了锁...");
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 解决穿透专用
     *
     * @param id  id
     * @param key key
     * @return shop
     */
    private T getFromDbAndWriteToCache(Long id, String key, Function<Long, T> getById) {
        // 缓存不存在
        // 使用缓存空对象的逻辑
        logger.debug("getFromDbAndWriteToCache");
        T t;
        Long ttl = cacheConstants.cacheNullTtl();
        String tJson = "";
        // 数据库查
        logger.debug("从数据库查" + entityName + ":" + id);
        t = getById.apply(id);
        if (t != null) {
            // 存在,写入Cache,更改TTL
            logger.debug("数据库中存在" + entityName + ":" + id);
            tJson = JSONUtil.toJsonStr(t);
            ttl = cacheConstants.cacheTtl();
        } else {
            logger.error("数据库中不存在" + entityName + ":" + id + ",是假数据");
        }
        stringRedisTemplate.opsForValue().set(key, tJson);
        stringRedisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        return t;
    }

    /**
     * 对于击穿,使用了逻辑失效的检查
     *
     * @param id id
     * @return shop
     */
    private T getHashFromCache(Long id) {
        logger.debug("getHashFromCache");
        T t = null;
        String key = cacheConstants.hotKey() + id;
        // 从缓存查
        logger.debug("从缓存查" + entityName + ":" + id);
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(key);
        if (map.isEmpty()) {
            logger.error(entityName + ":" + id + "在缓存中完全失效");
            // 什么情况, shop完完全全的消失了?
            map.put(cacheConstants.hotDataField(), "{}"); // 表示需要从数据库查
            map.put(cacheConstants.hotExpireField(), "0");
        } else {
            logger.debug("缓存中存在" + entityName + ":" + id);
        }
        String json = String.valueOf(map.get(cacheConstants.hotDataField()));
        long expire = Long.parseLong(String.valueOf(map.get(cacheConstants.hotExpireField())));
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));

        if (expire >= timestamp) {
            // 缓存未失效
            if (json.isEmpty()) {
                // 假数据, 且是未失效的假数据
                logger.error(entityName + ":" + id + "是我们未失效的假数据");
                // 我们的假数据, 为了应对穿透
                return null;
            }
            logger.debug("缓存" + entityName + ":" + id + "未失效");
            logger.debug("返回旧数据");
            t = JSONUtil.toBean(json, entityType);
            return t;
        } else if (expire != 0L) {
            logger.debug(entityName + ":" + id + "过期但存在");
        }
        // map.isEmpty()或过期的情况,都需要从数据库查询
        logger.debug(entityName + ":" + id + "是map.isEmpty()或过期的情况,都需要从数据库查询");
        if (json.isEmpty()) {
            // 假数据, 且是失效的假数据
            logger.error(entityName + ":" + id + "是我们的失效了的假数据,更新时间,等待中");
            // 我们的假数据, 为了应对穿透
            saveNullToCache(id);
            logger.debug("更新假数据完成");
            return null;
        }
        t = JSONUtil.toBean(json, entityType);
        t.setId(0L);
        return t;
    }
    private Long plusRandomSec(Long ttl ){
        long random;
        if (ttl<=10L){
            random = 0;
        }else{
            long exSec = ttl / 10;
            random = RandomUtil.randomLong(-exSec, exSec);
        }
        LocalDateTime time = LocalDateTime.now().plusSeconds(ttl + random);
        return time.toEpochSecond(ZoneOffset.of("+8"));
    }
    private void saveNullToCache(Long id){
        Long timestamp = plusRandomSec(cacheConstants.cacheNullTtl());
        stringRedisTemplate.opsForHash().putAll(cacheConstants.hotKey()+id,
                Map.of(cacheConstants.hotDataField(),"",cacheConstants.hotExpireField(),String.valueOf(timestamp)));
    }

    public T queryMutexFixByLogicalTtl(Long id, Function<Long, T> getById) {
        logger.debug("queryMutexFixByLogicalTtl");
        T t = getHashFromCache(id);
        if (t == null/*假数据*/ || t.getId() != 0L/*真数据*/) {
            logger.debug("缓存" + entityName + ":" + id + "没过期");
            return t;
        }
        /*数据过期*/
        logger.debug("缓存" + entityName + ":" + id + "过期了");
        String lockKey = cacheConstants.lockKey() + id;
        if (lock(lockKey)) {
            // 如果锁成功应该再次检测redis缓存是否过期
            // 做DoubleCheck
            // 如果没过期则无需重建
            // 缓存失效
            logger.debug("进入锁");
            t = getHashFromCache(id);
            if (t == null || t.getId() != 0L) {
                logger.debug("进行二次确认后" + entityName + ":" + id + "已恢复");
                return t;
            }
            logger.error("二次确认后" + entityName + ":" + id + "依旧没恢复");
            try {
                new Thread(() -> {
                    logger.debug("添加数据+" + entityName + ":" + id + "+到缓存的线程");
                    saveToCache(id, getById);
                    unlock(lockKey);
                }).start();// 用线程池
            } catch (Exception e) {
                logger.error("发生错误了, 但还是释放了锁....");
                unlock(lockKey);
                throw new RuntimeException(e);
            }
        }
        return t;
    }

    private void saveToCache(Long id, Function<Long, T> getById) {
        logger.debug("saveToCache");
        // 查询店铺信息
        T t = getById.apply(id);
        String key = cacheConstants.hotKey() + id;
        String json = "";
        long timestamp = cacheConstants.cacheNullTtl(); // 为解决穿透的假数据
        if (t != null) {
            logger.debug(entityName + ":" + id + "不是假数据");
            json = JSONUtil.toJsonStr(t);
            timestamp = plusRandomSec(cacheConstants.hotExpire());
        } else {
            logger.error(entityName + ":" + id + "是假数据");
        }
        stringRedisTemplate.opsForHash().putAll(key, Map
                .of(
                        cacheConstants.hotDataField(), json,
                        cacheConstants.hotExpireField(), String.valueOf(timestamp)
                )
        );
    }

    private boolean lock(String lockKey) {
        logger.debug("lock");
        Boolean exit = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "", cacheConstants.lockTtl(), TimeUnit.SECONDS);
        // 锁的时效设置成业务完成时间的十倍二十倍, 防止意外
        return exit != null && exit;
    }

    private void unlock(String lockKey) {
        logger.debug("unlock");
        stringRedisTemplate.delete(lockKey);
    }

    @Transactional// 完全忘了有这么一回事
    public boolean updateCache(T entity, Function<T, Boolean> updateById) {
        logger.debug("updateCache");
        if (entity == null || entity.getId() == null) {
            logger.error("传入不存在不合理的数据");
            return false;
        }
        // 1. 更新数据库
        Boolean exit = updateById.apply(entity);
        if (exit == null || !exit) {
            logger.error("未正常在数据库更新数据" + entityName + ":" + entity.getId());
            return false;
        }
        // 2. 删除缓存
        logger.debug("删除缓存" + entityName + ":" + entity.getId());
        stringRedisTemplate.delete(cacheConstants.cacheKey() + entity.getId());
        logger.debug("更新成功");
        return true;
    }
}
