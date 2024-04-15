package com.mysystem.futuresystemhd.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mysystem.futuresystemhd.domain.User;
import com.mysystem.futuresystemhd.service.UserService;

import javax.annotation.Resource;

public class AutomaticUtil {


    public static String getAccount(UserService userService) {
        int num =7;
        int k = 0;
        int z = 0;
        while (true){
            String[] vec = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
            num = num <= 0 ? 1 : num;
            StringBuffer str = new StringBuffer(10);
            for (int i = 0; i < num; i++) {
                int r1 = Long.valueOf(Math.round(Math.random() * (vec.length - 1))).intValue();
                str.append(vec[r1]);
            }
            String userAccount = str.toString();
            Long count = userService.count(new QueryWrapper<User>().eq("user_account", userAccount));
            if(count == 0){
                return userAccount;
            }
            k++;
            if(k == 10){
                k = 0;
                num++;
                if(z == 10){
                    return null;
                }
                z++;
            }
        }

    }


}
