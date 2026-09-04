package com.tanwei.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tanwei.dto.LoginFormDTO;
import com.tanwei.dto.Result;
import com.tanwei.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);
    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();
}
