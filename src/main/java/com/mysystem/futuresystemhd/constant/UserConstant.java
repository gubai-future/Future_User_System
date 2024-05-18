package com.mysystem.futuresystemhd.constant;

public class UserConstant {

    /**
     * 邮箱验证码大小
     */
    public static final int EMAIL_CAPTCHA_SIZE = 6;

    /**
     * 邮箱消息注册
     */
    public static final String EMAIL_USER_CAPTCHA_REGISTER = "email:user:captcha:register:";
    /**
     * 邮箱消息登录
     */
    public static final String EMAIL_USER_CAPTCHA_LOGIN = "email:user:captcha:login:";
    /**
     * 邮箱消息绑定
     */
    public static final String EMAIL_USER_CAPTCHA_BIND = "email:user:captcha:bind:";

    /**
     * 用户名正则表达式( 4 到 16 个字符的用户名，可以包含字母、数字和下划线)
     */
    public static final String USER_NAME_CHECK = "^[a-zA-Z0-9_]{4,16}$";

    /**
     * 邮箱正则表达式
     */
    public static final String USER_EMAIL_CHECK = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9]{2,}$";

    /**
     * 手机号正则表达式
     */
    public static final String USER_PHONE_CHECK = "^1[3-9]\\d{9}$";


    /**
     * 普通用户
     */
    public static final Integer USER_ORDINARY = 0;

    /**
     * 会员
     */
    public static final Integer USER_ORDINARY_PLUS = 1;

    /**
     * 管理员
     */
    public static final Integer USER_ADMIN = 2;

    /**
     * 超级管理员
     */
    public static final Integer USER_ADMIN_PLUS = 3;

    /**
     * 封禁
     */
    public static final Integer USER_SEALING = 1;
}
