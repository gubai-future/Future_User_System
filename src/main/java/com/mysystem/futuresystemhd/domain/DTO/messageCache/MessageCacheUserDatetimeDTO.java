package com.mysystem.futuresystemhd.domain.DTO.messageCache;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@ApiModel("指定私聊根据日期查询消息缓存")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageCacheUserDatetimeDTO {

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "开始日期",required = true)
    private LocalDateTime startDataTime;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "结束日期",required = true)
    private LocalDateTime enderDateTime;

    /**
     * 联系人id
     */
    @ApiModelProperty(value = "联系人id",required = true)
    private Long contactsId;

}
