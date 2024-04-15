package com.mysystem.futuresystemhd.exception;

import com.mysystem.futuresystemhd.annotation.Authority;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.constant.AuthorityConstant;

public class BusinessException extends RuntimeException{

    private Integer code;

    private String description;

    public BusinessException(Integer code,String msg,String description){
        super(msg);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode,String description){
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
