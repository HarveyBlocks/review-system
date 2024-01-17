package com.harvey.review_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.harvey.review_system.entity.ShopType;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {

    List<ShopType> sortShopList();
}
