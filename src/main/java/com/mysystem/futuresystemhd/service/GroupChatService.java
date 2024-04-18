package com.mysystem.futuresystemhd.service;

import com.mysystem.futuresystemhd.domain.DTO.groupChat.AddGroupChatDTO;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.AddGroupChatExamineDTO;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.ExamineGroupChatDTO;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.UpdateGroupChatDTO;
import com.mysystem.futuresystemhd.domain.GroupChat;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mysystem.futuresystemhd.domain.VO.GroupChatExamineVO;
import com.mysystem.futuresystemhd.domain.VO.UserVO;


public interface GroupChatService extends IService<GroupChat> {

    boolean insertGroupChat(AddGroupChatDTO addGroupChatDTO, UserVO request);

    boolean updateGroupChat(UpdateGroupChatDTO updateGroupChatDTO, UserVO loginUser);

    GroupChatExamineVO addExamine(AddGroupChatExamineDTO addGroupChatExamineDTO);

    GroupChatExamineVO examineGroupChat(ExamineGroupChatDTO examineGroupChatDTO, UserVO loginUser);
}
