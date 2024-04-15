package com.mysystem.futuresystemhd.service;

import com.mysystem.futuresystemhd.domain.DTO.groupChat.AddGroupChatDTO;
import com.mysystem.futuresystemhd.domain.GroupChat;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mysystem.futuresystemhd.domain.VO.UserVO;


public interface GroupChatService extends IService<GroupChat> {

    boolean insertgroupChat(AddGroupChatDTO addGroupChatDTO, UserVO request);
}
