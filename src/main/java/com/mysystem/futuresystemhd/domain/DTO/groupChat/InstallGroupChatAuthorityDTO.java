package com.mysystem.futuresystemhd.domain.DTO.groupChat;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("群主修改用户权限请求体")
public class InstallGroupChatAuthorityDTO implements Serializable
{

    @TableField(exist = false)
    private static final long serialVersionUID = 2522165753331980313L;

    /**
     * 修改的群聊
     */
    @ApiModelProperty(value = "修改的群聊",required = true)
    private Long groupChatId;

    /**
     * 修改的用户
     */
    @ApiModelProperty(value = "修改的用户",required = true)
    private Long userId;

    /**
     * 修改的身份
     */
    @ApiModelProperty(value = "修改的身份(0-成员 1-管理员)",required = true)
    private Integer authority;


}
