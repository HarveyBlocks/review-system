package com.harvey.review_system.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.harvey.review_system.entity.User;
import com.harvey.review_system.mapper.UserMapper;
import com.harvey.review_system.service.IUserService;
import com.harvey.review_system.utils.RegexUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Override
    public String sendCode(String phone) {
        String code = null;
        if(RegexUtils.isPhoneEffective(phone)){
            code = RandomUtil.randomNumbers(/*length*/ 6);
            // TODO 发送短信验证码
            // 好像要和什么什么合作啊,我不到啊,搞个假的
            log.debug("\n尊敬的"+phone+"用户:\n\t您的短信验证码是: "+code);
            // log是MP的ServiceImpl里的
        }
        return code;//单一出口
    }

    @Override
    public User loginByCode(String codeCache, String phone, String code) {

        // code的长度已经正确,code不为null
        if (!code.equals(codeCache)){
            return null;
        }

        // 如果验证码手机号一致, 去数据库查找用户
        User user = selectByPhone(phone);

        // 判断用户是否存在
        if(user==null ){
            // 不存在就创建新用户并保存
            user = new User();
            user.setPhone(phone);
            //newUser.setId()主键会自增, 不必管他
            // user.setIcon(User.DEFAULT_ICON);//头像使用默认的
            user.setNickName(User.DEFAULT_NICKNAME);//昵称使用默认的
            // 随机生成或直接为null,为null就百分百无法通过密码登录了.
            // 随机可能被猜中?
            user.setPassword(null);
            user.setUpdateTime(LocalDateTime.now());
            //这里就先不要增改扰了人家数据库清静
            baseMapper.insert(user);
            // user为null, user的id怎么确认? 再查一次? 太反人类了吧
            user = selectByPhone(phone);
        }
        // log.debug(String.valueOf(user));
        // 返回user
        return user;
    }

    @Override
    public User loginByPassword(String phone, String password) {
        // 依据电话号码从service取数据
        User user = selectByPhone(phone);
        // 取出来的数据和密码作比较
        if (user==null){
            User nullUser = new User();
            nullUser.setId(-1L);
            return nullUser;//用户名不存在
        }
        if (!password.equals(user.getPassword())){
            // password经过检验, 非null, 数据库里的password可能是null
            return null;//用户名或密码错误
        }
        // log.debug(String.valueOf(user));
        // 正确则返回user值
        return user;
    }

    private User selectByPhone(String phone) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select().eq(User::getPhone, phone);
        return baseMapper.selectOne(lambdaQueryWrapper);
    }


}
