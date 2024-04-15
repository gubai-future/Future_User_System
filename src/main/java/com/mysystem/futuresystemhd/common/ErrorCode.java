package com.mysystem.futuresystemhd.common;

public enum ErrorCode {
    SUCCESS(200,"success","成功"),
    REQUEST_IS_NULL(40001,"请求数据为空",""),
    LENGTH_IS_ERROR(40002,"长度错误",""),
    LOGIN_IS_NULL(40003,"未登录",""),
    NOT_AUTHORITY(40004,"无权限",""),
    REQUEST_IS_ERROR(40005,"请求失败",""),
    REGISTER_ERROR(40006,"注册失败",""),
    LOGIN_ERROR(40007,"登录失败",""),
    SYSTEM_IS_ERROR(50000,"系统内部错误","");

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 状态信息
     */
    private String msg;

    /**
     * 详情
     */
    private String description;


    ErrorCode(Integer code, String msg, String description) {
        this.code = code;
        this.msg = msg;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getDescription() {
        return description;
    }
}
