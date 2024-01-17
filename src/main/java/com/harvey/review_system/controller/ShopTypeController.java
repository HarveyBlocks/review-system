package com.harvey.review_system.controller;


import com.harvey.review_system.dto.Result;
import com.harvey.review_system.entity.ShopType;
import com.harvey.review_system.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {

        List<ShopType> shopTypeList = typeService.sortShopList();
        return Result.ok(shopTypeList);
    }
}
