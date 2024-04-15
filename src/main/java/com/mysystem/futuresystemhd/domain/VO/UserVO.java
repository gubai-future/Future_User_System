package com.mysystem.futuresystemhd.domain.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "UserVO",description = "用户响应体")
public class UserVO implements Serializable {
    /**
     * 编号
     */
    @ApiModelProperty(value = "编号")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账户
     */
    @ApiModelProperty(value = "账户")
    private String userAccount;


    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String userName;

    /**
     * 性别 0-男 1-女 2-未知
     */
    @ApiModelProperty(value = "性别 0-男 1-女 2-未知")
    private Integer userSex;

    /**
     * 年龄
     */
    @ApiModelProperty(value = "年龄")
    private Integer userAge;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;


    /**
     * 用户身份 0-普通用户 1-会员 2-管理员 3-超级管理员(唯一)
     */
    @ApiModelProperty(value = "用户身份 0-普通用户 1-会员 2-管理员 3-超级管理员(唯一)")
    private Integer userRole;


    /**
     * 是否封禁
     */
    private Integer closeStatic;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    /**
     * 用户标签
     */
    @ApiModelProperty(value = "用户标签")
    private String userTags;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}