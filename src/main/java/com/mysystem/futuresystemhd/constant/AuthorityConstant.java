package com.mysystem.futuresystemhd.constant;

public enum AuthorityConstant {
    STATUS_NORMAL(0,"正常"),
    STATUS_BAN(1,"封禁"),
    USER_ORDINARY(0,"普通用户"),
    USER_MEMBER(1,"会员"),
    USER_ADMIN(2,"管理员"),
    USER_ADMIN_PLUS(3,"超级管理员"),
    GROUP_CHAT_ADMIN(1,"群聊管理员"),
    GROUP_CHAT_MASTER(2,"群主"),
    GROUP_CHAT_PUBLIC(0,"公开"),
    GROUP_CHAT_PRIVATE(1,"私密");


    private Integer statusId;

    private String status;


    AuthorityConstant(Integer statusId, String status) {
        this.statusId = statusId;
        this.status = status;
    }

    public static String getByStatus(AuthorityConstant authorityConstant){
        return authorityConstant.getStatus();
    }

    public static Integer getByStatusId(AuthorityConstant authorityConstant){
        return authorityConstant.getStatusId();
    }

    public static AuthorityConstant  getByAuthority(Integer statusId){
        AuthorityConstant[] values = AuthorityConstant.values();
        for (AuthorityConstant value : values) {
            if(value.getStatusId().equals(statusId)){
                return value;
            }
        }
        return null;
    }

    public static String getByStatusId(Integer statusId){
        AuthorityConstant[] values = AuthorityConstant.values();
        for (AuthorityConstant value : values) {
            if(value.getStatusId().equals(statusId)){
                return value.status;
            }
        }
        return null;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public String getStatus() {
        return status;
    }
}
