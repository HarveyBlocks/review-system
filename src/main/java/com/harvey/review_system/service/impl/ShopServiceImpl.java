package com.harvey.review_system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.harvey.review_system.entity.Shop;
import com.harvey.review_system.mapper.ShopMapper;
import com.harvey.review_system.service.IShopService;
import com.harvey.review_system.utils.CacheClient;
import com.harvey.review_system.utils.ShopCacheConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final CacheClient<Shop> shopCacheClient;

    public ShopServiceImpl(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
        this.shopCacheClient = new CacheClient<>(stringRedisTemplate,Shop.class,log,new ShopCacheConstants());
    }

    @Override
    public Shop queryById(Long id) {
        return shopCacheClient.queryById(id,n->{
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
            log.debug("开始在数据库中查询shop:"+id);
            return this.getById(n);
        });
    }

    @Override
    public Shop queryMutexFixByLock(Long id) {
        return shopCacheClient.queryMutexFixByLock(id,n->{
            log.debug("模拟漫长的查询过程");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
            log.debug("开始在数据库中查询shop:"+id);
            return this.getById(n);
        });
    }

    @Override
    public Shop queryMutexFixByLogicalTtl(Long id) {
        return shopCacheClient.queryMutexFixByLogicalTtl(id,n->{
            log.debug("模拟漫长的查询过程");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
            log.debug("开始在数据库中查询shop:"+id);
            return this.getById(n);
        });
    }

    @Override
    public boolean updateCache(Shop shop) {
        return shopCacheClient.updateCache(shop,entity->{
            log.debug("模拟漫长的查询过程");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
            log.debug("开始在数据库中更新shop:"+entity.getId());
            return this.updateById(entity);
        });
    }

    /*@Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Shop queryById(Long id) {
        Shop shop = null;
        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;

        // 从缓存查
        String json = stringRedisTemplate.opsForValue().get(shopKey);
        if (json != null) {
            // 缓存存在
            if (json.isEmpty()) {
                // 我们的假数据
                return null;
            }
            shop = JSONUtil.toBean(json, Shop.class);
            return shop;
        }
        // 缓存不存在
        return getShopFromDbAndWriteToRedis(id, shopKey);
    }

    @Override
    public Shop queryMutexFixByLock(Long id) {
        Shop shop = null;
        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
        while (true) {
            // 从缓存查
            String json = stringRedisTemplate.opsForValue().get(shopKey);
            if (json != null) {
                // 缓存存在
                if (json.isEmpty()) {
                    // 我们的假数据, 为了应对穿透
                    return null;
                }
                shop = JSONUtil.toBean(json, Shop.class);
                return shop;
            }
            String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
            try {
                if (lock(lockKey)) {// 每个店铺要有自己的锁
                    shop = getShopFromDbAndWriteToRedis(id, shopKey);
                    // 完成读取要释放锁
                    unlock(lockKey);
                    return shop;
                } else {
                    // 线程需要等待
                    Thread.sleep(100);
                    // 没出现问题, 不是做读写操作的, 不需要释放锁
                }
            } catch (Exception e) {
                // 发生问题要释放锁
                unlock(lockKey);
                throw new RuntimeException(e);
            }
        }
    }

    *//**
     *  解决穿透专用
     * @param id id
     * @param shopKey key
     * @return shop
     *//*
    private Shop getShopFromDbAndWriteToRedis(Long id, String shopKey) {
        // 缓存不存在
        // 使用缓存空对象的逻辑
        Shop shop;
        Long ttl = RedisConstants.CACHE_NULL_TTL;
        String shopJson = "";
        // 数据库查
        shop = this.getById(id);
        if (shop != null) {
            // 存在,写入Redis,更改TTL
            shopJson = JSONUtil.toJsonStr(shop);
            ttl = RedisConstants.CACHE_SHOP_TTL;
        }
        stringRedisTemplate.opsForValue().set(shopKey, shopJson);
        stringRedisTemplate.expire(shopKey, ttl, TimeUnit.MINUTES);
        return shop;
    }

    *//**
     * 对于击穿,使用了逻辑失效的检查
     * @param id id
     * @return shop
     *//*
    private Shop getHashShopFromRedis(Long id) {
        Shop shop = null;
        String shopKey = RedisConstants.HOT_SHOP_KEY + id;
        // 从缓存查
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(shopKey);
        if (map.isEmpty()) {
            // 什么情况, shop完完全全的消失了?
            map.put(RedisConstants.HOT_SHOP_DATA_FIELD, "{}"); // 表示需要从数据库查
            map.put(RedisConstants.HOT_SHOP_EXPIRE_FIELD, "0");
        }
        String json = String.valueOf(map.get(RedisConstants.HOT_SHOP_DATA_FIELD));
        long expire = Long.parseLong(String.valueOf(map.get(RedisConstants.HOT_SHOP_EXPIRE_FIELD)));
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));

        if (expire >= timestamp) {
            // 缓存未失效
            if (json.isEmpty()) {
                // 我们的假数据, 为了应对穿透
                return null;
            }
            shop = JSONUtil.toBean(json, Shop.class);
            return shop;
        }
        // map.isEmpty()或过期的情况,都需要从数据库查询
        shop = JSONUtil.toBean(json, Shop.class);
        shop.setId(0L);
        return shop;
    }

    @Override
    public Shop queryMutexFixByLogicalTtl(Long id) {
        Shop shop = getHashShopFromRedis(id);
        if (shop == null*//*假数据*//* || shop.getId() != 0L*//*真数据*//* ) {
            return shop;
        }
        *//*数据过期*//*
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        if (lock(lockKey)) {
            // 如果锁成功应该再次检测redis缓存是否过期
            // 做DoubleCheck
            // 如果没过期则无需重建
            // 缓存失效
            shop = getHashShopFromRedis(id);
            if (shop == null || shop.getId() != 0L) {
                return shop;
            }
            try {
                new Thread(() -> {
                    saveShopToRedis(id, RedisConstants.HOT_SHOP_EXPIRE);//5秒过期,方便测试
                    unlock(lockKey);
                }).start();// 用线程池
            } catch (Exception e) {
                unlock(lockKey);
                throw new RuntimeException(e);
            }
        }
        return shop;
    }

    private void saveShopToRedis(Long id, long expireSecond) {
        // 查询店铺信息
        Shop shop = getById(id);
        String key = RedisConstants.HOT_SHOP_KEY + id;
        String json = "";
        long timestamp = RedisConstants.CACHE_NULL_TTL; // 为解决穿透的假数据
        if(shop!=null ){
            json = JSONUtil.toJsonStr(shop);
            long exSec = expireSecond / 10;
            long random = exSec>0L?RandomUtil.randomLong(-exSec, exSec):0;
            LocalDateTime time = LocalDateTime.now().plusSeconds(expireSecond + random);
            timestamp = time.toEpochSecond(ZoneOffset.of("+8"));
        }
        stringRedisTemplate.opsForHash().putAll(key, Map
                .of(
                        RedisConstants.HOT_SHOP_DATA_FIELD, json,
                        RedisConstants.HOT_SHOP_EXPIRE_FIELD, String.valueOf(timestamp)
                )
        );
    }

    private boolean lock(String lockKey) {
        Boolean exit = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);
        // 锁的时效设置成业务完成时间的十倍二十倍, 防止意外
        return exit != null && exit;
    }

    private void unlock(String lockKey) {
        stringRedisTemplate.delete(lockKey);
    }

    @Override
    @Transactional// 完全忘了有这么一会儿事
    public boolean updateCache(Shop shop) {
        if (shop==null||shop.getId() == null) {
            return false;
        }
        // 1. 更新数据库
        if (!updateById(shop)) {
            return false;
        }
        // 2. 删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shop.getId());
        return true;
    }*/
}
