package com.tanwei.utils;

import com.tanwei.dto.UserDTO;
import com.tanwei.entity.User;

/**
 * UserHolder 类的主要作用是通过 ThreadLocal 实现线程安全的用户信息存储和访问，适用于多线程环境下的用户会话管理。
 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
