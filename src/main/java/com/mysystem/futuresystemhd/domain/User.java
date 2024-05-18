package com.mysystem.futuresystemhd.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    /**
     * 编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账户
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 性别 0-男 1-女 2-未知
     */
    private Integer userSex;

    /**
     * 年龄
     */
    private Integer userAge;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 是否封禁
     */
    private Integer closeStatic;

    /**
     * 是否删除 0-未删除 1-删除
     */
    private Integer isDelete;

    /**
     * 用户身份 0-普通用户 1-会员 2-管理员 3-超级管理员(唯一)
     */
    private Integer userRole;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户标签
     */
    private String userTags;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}