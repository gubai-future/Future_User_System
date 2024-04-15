package com.mysystem.futuresystemhd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.constant.AccountConstant;
import com.mysystem.futuresystemhd.constant.AuthorityConstant;
import com.mysystem.futuresystemhd.constant.LockConstant;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.AddGroupChatDTO;
import com.mysystem.futuresystemhd.domain.GroupChat;
import com.mysystem.futuresystemhd.domain.UserGroupChatInfo;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.service.GroupChatService;
import com.mysystem.futuresystemhd.mapper.GroupChatMapper;
import com.mysystem.futuresystemhd.service.UserGroupChatInfoService;
import com.mysystem.futuresystemhd.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class GroupChatServiceImpl extends ServiceImpl<GroupChatMapper, GroupChat>
        implements GroupChatService {

    @Resource
    private UserService userService;

    @Resource
    private GroupChatMapper groupChatMapper;

    @Resource
    private UserGroupChatInfoService userGroupChatInfoService;

    @Resource
   private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertgroupChat(AddGroupChatDTO addGroupChatDTO, UserVO loginUser) {

        Long LoginUserid = loginUser.getId();

        if(LoginUserid == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        String string = LoginUserid.toString();

        String format = LockConstant.USER_LOCK + string;

        RLock lock = redissonClient.getLock(format);

        try {
            if(lock.tryLock(0,-1, TimeUnit.SECONDS)){
                if (addGroupChatDTO == null) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
                }

                //判断用户是否正常
                Integer closeStatic = loginUser.getCloseStatic();
                if (AuthorityConstant.STATUS_BAN.equals(AuthorityConstant.getByStatusId(closeStatic))) {
                    throw new BusinessException(ErrorCode.NOT_AUTHORITY);
                }

                //群名是否重复
                String name = addGroupChatDTO.getName();
                Long groupChatNum = groupChatMapper.selectCount(new QueryWrapper<GroupChat>().eq("name",name));
                if (groupChatNum >= 1) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR, "群名重复");
                }

                //群聊名正则校验
                Matcher nameMatcher = Pattern.compile(AccountConstant.groupChat_NAME).matcher(name);

                if(!nameMatcher.find()){
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //最多只能创建5个群聊
                long count = userGroupChatInfoService.count(new QueryWrapper<UserGroupChatInfo>().eq("user_id", LoginUserid));
                if (count >= 5) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR, "最多只能创建5个群聊");
                }

                GroupChat groupChat = new GroupChat();

                BeanUtils.copyProperties(addGroupChatDTO, groupChat);

                //如果是管理员或者是会员
                Integer userRole = loginUser.getUserRole();
                if(AuthorityConstant.USER_MEMBER.equals(AuthorityConstant.getByStatusId(userRole)) || userService.isAdmin(loginUser)){
                    groupChat.setMaxPeopleNum(120);
                }

                groupChat.setUserId(LoginUserid);
                groupChat.setCurrentPeopleNum(1);
                groupChat.setCreateId(LoginUserid);
                groupChat.setUpdateId(LoginUserid);

                boolean groupChatResult = this.save(groupChat);

                Long NameCount = this.count(new QueryWrapper<GroupChat>().eq("name", name));

                if(NameCount >= 2 || !groupChatResult){
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                UserGroupChatInfo usergroupChatInfo = new UserGroupChatInfo();
                usergroupChatInfo.setUserId(LoginUserid);
                usergroupChatInfo.setGroupChatId(groupChat.getId());
                usergroupChatInfo.setGroupChatName(loginUser.getUserName());
                usergroupChatInfo.setAuthority(2);

                boolean userRanInfoResult = userGroupChatInfoService.save(usergroupChatInfo);

                if(!userRanInfoResult){
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                return true;
            }
        } catch (InterruptedException e) {
            log.error("创建队伍失败:",e);
            return false;
        } finally {
            lock.unlock();
        }

        return false;
    }
}




