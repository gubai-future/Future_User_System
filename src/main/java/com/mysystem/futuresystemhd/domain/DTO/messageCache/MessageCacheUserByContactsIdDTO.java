package com.mysystem.futuresystemhd.domain.DTO.messageCache;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageCacheUserByContactsIdDTO {

    @ApiModelProperty(value = "联系人id",required = true)
    private Long contactsId;
}
