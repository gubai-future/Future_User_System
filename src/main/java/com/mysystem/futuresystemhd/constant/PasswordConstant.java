package com.mysystem.futuresystemhd.constant;

public class PasswordConstant {

    public static final String PASSWORD_ENCRYPTION_Q = "the_is_my_future_system_%%";

    public static final String PASSWORD_ENCRYPTION_H = "I_IS_WORD_future&*&_system_@#";

    /**
     * 密码正则表达式(密码 8 ~ 15 位(必须至少含有大小写英文一位。特殊字符一位)
     */
    public static final String PASSWORD_CHECK = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{8,15}$";
}
