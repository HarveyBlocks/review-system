package com.harvey.review_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.harvey.review_system.entity.User;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    /**
     * 生成校验码并发送
     *
     * @param phone 手机号
     * @return 校验码
     */
    String sendCode(String phone);

    /**
     * 用验证码登录验证
     *
     * @param codeCache  会话保存的验证码
     * @param phone      新请求的手机号
     * @param code       新请求的验证码
     * @return 用户信息
     */
    User loginByCode( String codeCache, String phone, String code);

    /**
     * 校验用户名密码
     *
     * @param phone    电话号码, 也做账号
     * @param password 密码
     * @return 若返回id为-1的则为不存在用户,<br>
     * 若返回null则为用户名密码错误,<br>
     * 否则返回正确查到的用户<br>
     */
    User loginByPassword(String phone, String password);
}
