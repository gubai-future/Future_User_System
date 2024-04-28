package com.mysystem.futuresystemhd.domain.DTO.groupChat;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "退出群聊请求体")
public class QuitGroupChatDTO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 4900468734006671810L;

    /*@ApiModelProperty(value = "用户id",required = true)
    private Long userId;*/

    @ApiModelProperty(value = "群聊id",required = true)
    private Long groupChatId;
}
