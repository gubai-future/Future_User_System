package com.mysystem.futuresystemhd.constant;

public class UserConstant {

    /**
     * 用户名正则表达式( 4 到 16 个字符的用户名，可以包含字母、数字和下划线)
     */
    public static final String USER_NAME_CHECK = "^[a-zA-Z0-9_]{4,16}$";

    /**
     * 邮箱正则表达式
     */
    public static final String USER_EMAIL_CHECK = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

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

    public static final Integer USER_FENGJING = 1;
}
