package com.gamesky.card.service.impl;

import com.gamesky.card.core.Constants;
import com.gamesky.card.core.Keyable;
import com.gamesky.card.core.Marshaller;
import com.gamesky.card.core.Page;
import com.gamesky.card.core.exceptions.CheckCodeInvalidException;
import com.gamesky.card.core.exceptions.CheckCodeWrongException;
import com.gamesky.card.core.exceptions.MarshalException;
import com.gamesky.card.core.model.User;
import com.gamesky.card.core.model.UserExample;
import com.gamesky.card.dao.mapper.UserMapper;
import com.gamesky.card.service.UserService;
import com.gamesky.card.service.key.CheckCodeKey;
import com.gamesky.card.service.key.LoginKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created on 2/22/15.
 *
 * @Author lianghongbin
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Marshaller<Keyable, String> marshaller;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 保存用户、添加用户
     *
     * @param user 用户对象
     * @return 影响条数
     */
    @Override
    public int save(User user) {
        return userMapper.insert(user);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 影响条数
     */
    @Override
    public int delete(int id) {
        return userMapper.deleteByPrimaryKey(id);
    }

    /**
     * 更新用户
     *
     * @param user 用户对象
     * @return 影响条数
     */
    @Override
    public int update(User user) {
        return userMapper.updateByPrimaryKey(user);
    }

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return 查找到的用户
     */
    @Override
    public User find(int id) {
        return userMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据用户唯一手机查找用户
     *
     * @param phone 用户手机
     * @return 影响条数
     */
    @Override
    public User findByPhone(String phone) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andPhoneEqualTo(phone);
        List<User> users = userMapper.selectByExample(userExample);
        if (users == null || users.size() == 0) {
            return null;
        }

        return users.get(0);
    }

    /**
     * 分页查找所有用户
     *
     * @param page          分页参数
     * @param orderByClause 排序
     * @return 用户列表
     */
    @Override
    public List<User> findAll(Page page, String orderByClause) {
        UserExample userExample = new UserExample();
        userExample.setLimitOffset(page.getOffset());
        userExample.setLimit(page.getSize());
        if (orderByClause != null) {
            userExample.setOrderByClause(orderByClause);
        }

        return userMapper.selectByExample(userExample);
    }

    /**
     * 查找所有用户数
     *
     * @return 用户数
     */
    @Override
    public int findCount() {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdGreaterThan(0);
        return userMapper.countByExample(userExample);
    }

    /**
     * 根据条件分页查找用户
     *
     * @param userExample 查找条件
     * @return 用户列表
     */
    @Override
    public List<User> findByCondition(UserExample userExample) {
        return userMapper.selectByExample(userExample);
    }

    /**
     * 根据条件查找用户数
     *
     * @param userExample 查找条件
     * @return 用户数
     */
    @Override
    public int findCountByCondition(UserExample userExample) {
        return userMapper.countByExample(userExample);
    }

    /**
     * 根据用户手机号，判断用户是否在登录状态
     *
     * @param phone 手机号
     * @return true/false
     */
    @Override
    public boolean isLogin(final String phone) {
        String result;
        try {
            result = marshaller.unmarshal(new LoginKey(phone, 24 * 60 * 60));
        } catch (MarshalException e) {
            logger.error("验证用户是否登录出错：{}", e);
            return false;
        }

        return StringUtils.isNotBlank(result);
    }

    /**
     * 使用手机号和验证码进行登录
     *
     * @param phone     手机号
     * @param checkCode 验证码
     * @return true/false
     */
    @Override
    public boolean login(final String phone, String checkCode) throws CheckCodeInvalidException, CheckCodeWrongException {

        String code;
        try {
            code = marshaller.unmarshal(new CheckCodeKey(phone, 60));

            if (code == null) {
                throw new CheckCodeInvalidException("验证码已过期");
            }

            if (!checkCode.equalsIgnoreCase(code)) {
                throw new CheckCodeWrongException("验证码错误");
            }
        } catch (MarshalException e) {
            logger.error("校验验证码失败");
            return false;
        }

        try {
            marshaller.marshal(new LoginKey(phone, 24*60*60), checkCode);
        } catch (MarshalException e) {
            logger.error("用户登录出错：{}", e);
            return false;
        }

        return isLogin(phone);
    }
}
