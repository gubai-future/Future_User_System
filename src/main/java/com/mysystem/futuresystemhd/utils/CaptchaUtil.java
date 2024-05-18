package com.mysystem.futuresystemhd.utils;

import java.util.Random;

public class CaptchaUtil {

    public static String RandomNumber(int lengthMax){
        Random random = new Random();

        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < lengthMax; i++) {
            int randomNum = random.nextInt(10);
            stringBuffer.append(randomNum);
        }

        return stringBuffer.toString();
    }
}
