package com.harvey.review_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.harvey.review_system.entity.Shop;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    /**
     * 解决穿透
     * @param id id
     * @return shop
     */
    Shop queryById(Long id) ;

    /**
     * 使用互斥锁解决击穿, 同时也解决穿透
     * @param id id
     * @return shop
     */
    Shop queryMutexFixByLock(Long id);


    /**
     * 使用逻辑过期解决击穿, 同时解决穿透, 解决雪崩
     * @param id id
     * @return shop
     */
    Shop queryMutexFixByLogicalTtl(Long id);

    /**
     * 更新数据. 缓存更新策略,对于穿透问题会提供假数据.有事务
     * @param shop shop
     * @return true表示找到, false表示有人想穿透
     */
    boolean updateCache(Shop shop);


}
