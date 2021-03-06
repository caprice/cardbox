package com.gamesky.card.web.controller;

import com.gamesky.card.core.ResultGenerator;
import com.gamesky.card.core.exceptions.CheckCodeInvalidException;
import com.gamesky.card.core.exceptions.CheckCodeWrongException;
import com.gamesky.card.core.model.User;
import com.gamesky.card.service.CheckCodeService;
import com.gamesky.card.service.CodeGenerator;
import com.gamesky.card.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 选题控制器
 * Created on 3/13/15.
 *
 * @Author lianghongbin
 */
@Controller
@RequestMapping(value = "/1_0/user", produces="application/json;charset=UTF-8")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private CodeGenerator generator;
    @Autowired
    private CheckCodeService checkCodeService;

    /**
     * 系统登录
     * @param phone 登录手机号
     * @param checkCode 登录验证码
     * @return 返回text结果
     */
    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(String phone, String device, String checkCode) {
        User user;
        try {
            user = userService.login(phone, device, checkCode);
        } catch (CheckCodeInvalidException e) {
            return ResultGenerator.generateError("验证码已过期");
        } catch (CheckCodeWrongException e) {
            return ResultGenerator.generateError("验证码错误");
        }

        if (user == null) {
            return ResultGenerator.generateError("登录发生错误");
        }

        Map<String, User> params = new HashMap<>();
        params.put("user", user);
        return ResultGenerator.generate(params);
    }

    @ResponseBody
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(String phone) {
        boolean result = userService.logout(phone);
        if (result) {
            return ResultGenerator.generate();
        }

        return ResultGenerator.generateError("注销出错");
    }

    /**
     * 判断是否是在登录状态
     * @param phone 登录手机
     * @return 返回text结果
     */
    @ResponseBody
    @RequestMapping(value = "/islogin", method = RequestMethod.GET)
    public String isLogin(String phone) {
        if(userService.isLogin(phone)){
            return ResultGenerator.generate();
        }
        return ResultGenerator.generateError("该手机号未登录");
    }

    /**
     * 生成验证码
     * @param phone 手机号
     * @return 返回生成的难码
     */
    @ResponseBody
    @RequestMapping(value = "/checkcode", method = RequestMethod.GET)
    public String checkCode(final String phone) {
        String code = generator.generate();

        if (!checkCodeService.send(phone, code)) {
            return ResultGenerator.generateError("验证码发送或存储失败");
        }

        Map<String,String> data = new HashMap<>();
        data.put("checkCode", code);
        return ResultGenerator.generate(data);
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}
