package com.mysystem.futuresystemhd.utils;

import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.exception.BusinessException;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class DateTimeForUtils {

    private static final String ForeOne = "yyyy-MM-dd HH:mm:ss";

    public static String ForeDateTime(Date date) {

        if(date == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ForeOne);


        String format = simpleDateFormat.format(date);

        Date parse = null;

        try {

            parse = simpleDateFormat.parse(format);
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        return format;
    }


    /**
     * 将LocalDateTime的T去掉
     * @param localDateTime
     * @return
     */
    public static String DateTimeRemoveT(LocalDateTime localDateTime){
        if(localDateTime == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        String DateTime = localDateTime.toString();

        if (DateTime.contains("T")){
            DateTime = DateTime.replace("T"," ");
        }

        return DateTime;
    }

}
