package com.mysystem.futuresystemhd.utils;

import com.mysystem.futuresystemhd.constant.PasswordConstant;
import org.springframework.util.DigestUtils;

public class EncryptionUtil {

    public static String EncryptionPassword(String password){
        String EncryptionPassword = DigestUtils.md5DigestAsHex((PasswordConstant.PASSWORD_ENCRYPTION_Q + password + PasswordConstant.PASSWORD_ENCRYPTION_H).getBytes());
        return EncryptionPassword;
    }

}
