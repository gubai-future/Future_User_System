package com.mysystem.futuresystemhd.service;

import com.mysystem.futuresystemhd.domain.DTO.groupChat.*;
import com.mysystem.futuresystemhd.domain.GroupChat;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mysystem.futuresystemhd.domain.VO.GroupChatExamineVO;
import com.mysystem.futuresystemhd.domain.VO.GroupChatVO;
import com.mysystem.futuresystemhd.domain.VO.UserVO;

import java.util.List;


public interface GroupChatService extends IService<GroupChat> {

    boolean insertGroupChat(AddGroupChatDTO addGroupChatDTO, UserVO request);

    boolean updateGroupChat(UpdateGroupChatDTO updateGroupChatDTO, UserVO loginUser);

    GroupChatExamineVO addExamine(AddGroupChatExamineDTO addGroupChatExamineDTO);

    GroupChatExamineVO examineGroupChat(ExamineGroupChatDTO examineGroupChatDTO, UserVO loginUser);

    boolean joinGroupChat(Long groupChatId, UserVO loginUser);

    boolean QuitGroupChat(QuitGroupChatDTO quitGroupChatDTO, UserVO loginUser);

    List<GroupChatVO> selectByName(QueryGroupChatByNameDTO queryGroupChatByNameDTO);

    List<GroupChat> selectAdminBy(QueryGroupChatDTO queryGroupChatDTO, UserVO loginUser);

    boolean installGroupChatName(String name, Long groupChatId, UserVO loginUser);

    boolean installGroupChatAthority(InstallGroupChatAuthorityDTO installGroupChatAuthorityDTO, UserVO loginUser);
}
