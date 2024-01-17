package com.harvey.review_system.service.impl;

import cn.hutool.json.JSONUtil;
import com.harvey.review_system.entity.Shop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-01-05 17:23
 */
public class ClassTypeTest<T extends Object> {

    Class<T> clazz;
    private final Logger logger = LoggerFactory.getLogger(clazz + ":" + this.getClass().getSimpleName());


    public String show() {
        O o = new O();
        o.get(clazz);

        o.get(o.getClass());

        logger.info("name");

        return "name";
    }

}

class O {
    public static void main(String[] args) {

    }

    public <T> void get(Class<T> clazz) {
        System.out.println(clazz);
    }
}