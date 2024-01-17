package com.harvey.review_system.controller;


import cn.hutool.core.lang.UUID;
import com.harvey.review_system.dto.LoginFormDTO;
import com.harvey.review_system.dto.Result;
import com.harvey.review_system.dto.UserDTO;
import com.harvey.review_system.entity.User;
import com.harvey.review_system.entity.UserInfo;
import com.harvey.review_system.service.IUserInfoService;
import com.harvey.review_system.service.IUserService;
import com.harvey.review_system.utils.RedisConstants;
import com.harvey.review_system.utils.RegexUtils;
import com.harvey.review_system.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone) {
        // 发送短信验证码并保存验证码
        String code = userService.sendCode(phone);
        if (code == null) {
            return Result.fail("手机号不合法");
        }

        //session.setAttribute(CODE_SESSION_KEY,code);
        //session.setAttribute(PHONE_SESSION_KEY,phone);
        // 记得设置有效期
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        return Result.ok();
    }

    /**
     * 登录功能
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, @CookieValue("JSESSIONID") String sessionId) {
        //实现登录功能
        // System.out.println(result);
        return chooseLoginWay(loginForm, sessionId);//Result.fail("功能未完成");
    }

    private Result chooseLoginWay(LoginFormDTO loginForm, String sessionId) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        String password = loginForm.getPassword();
        if (phone == null || !RegexUtils.isPhoneEffective(phone)
            // 网上说参数校验放在controller, 这算参数校验吗?
        ) {
            return Result.fail("请正确输入电话号");
        }
        if ((password == null) == (code == null)) {
            // 无法决定是密码登录还是验证码登录的情况
            return Result.fail("请正确输入验证码或密码");
        }
        User user /* = null*/;

        if (code != null) {
            if (code.length() != 6) {
                return Result.fail("请输入正确格式的验证码");
            }
            // 使用验证码登录
            //Object phoneCache = session.getAttribute(PHONE_SESSION_KEY);
            //Object codeCache = session.getAttribute(CODE_SESSION_KEY);
            String codeCache = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
            if (codeCache == null || codeCache.isEmpty()) {
                return Result.fail("您的表单数据已丢失,请退出刷新重试");
            }
            user = userService.loginByCode(codeCache, phone, code);
            if (user == null) {
                return Result.fail("验证码不正确");

            } else {
                // 如果成功了, 就删除Redis缓存
                stringRedisTemplate.delete(RedisConstants.LOGIN_CODE_KEY + phone);
                //session.removeAttribute(CODE_SESSION_KEY);
                // 否则不删除会话,给用户一个再次输入验证码的机会
            }
        } else /*if(password!=null)*/ {
            user = userService.loginByPassword(phone, password);
            if (user == null) {
                return Result.fail("密码不正确");
            } else if (user.getId().equals(-1L)) {
                return Result.fail("该用户不存在");
            }
        }


        // session.setAttribute(Constants.USER_SESSION_KEY,new UserDTO(user.getId(),user.getNickName(),user.getIcon()));
        // 将用户DTO存入Redis
        String token = (sessionId != null ? sessionId :// 生成随机Token,hutool工具包
                UUID.randomUUID().toString(true));//true表示不带中划线;
        UserDTO userDTO = new UserDTO(user.getId(), user.getNickName(), user.getIcon());
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,
                new HashMap<>(Map.of(
                        "id", userDTO.getId().toString(),
                        "nickName", userDTO.getNickName(),
                        "icon", userDTO.getIcon())
                )
        );//减少请求次数
        // 设置有效期
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 返回token

        return Result.ok(token);
    }


    /**
     * 登出功能
     *
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request, HttpServletResponse response) {
        // TODO 实现登出功能
        String token = request.getHeader("authorization");//依据前端的信息
        if (null != token && !token.isEmpty()) {// 获取user数据
            String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
            stringRedisTemplate.delete(tokenKey);
            UserHolder.removeUser();
            response.setStatus(401);
//            return Result.ok();
        }
        return Result.ok();
    }

    @GetMapping("/me")
    public Result me() {
        // 获取当前登录的用户并返回
        return Result.ok(UserHolder.getUser());
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId) {
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }
}
