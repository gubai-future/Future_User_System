package com.mysystem.futuresystemhd.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.constant.AccountConstant;
import com.mysystem.futuresystemhd.constant.AuthorityConstant;
import com.mysystem.futuresystemhd.constant.ExamineConstant;
import com.mysystem.futuresystemhd.constant.LockConstant;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.AddGroupChatDTO;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.AddGroupChatExamineDTO;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.ExamineGroupChatDTO;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.UpdateGroupChatDTO;
import com.mysystem.futuresystemhd.domain.GroupChat;
import com.mysystem.futuresystemhd.domain.GroupChatExamine;
import com.mysystem.futuresystemhd.domain.User;
import com.mysystem.futuresystemhd.domain.UserGroupChatInfo;
import com.mysystem.futuresystemhd.domain.VO.GroupChatExamineVO;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.mapper.GroupChatExamineMapper;
import com.mysystem.futuresystemhd.service.GroupChatService;
import com.mysystem.futuresystemhd.mapper.GroupChatMapper;
import com.mysystem.futuresystemhd.service.UserGroupChatInfoService;
import com.mysystem.futuresystemhd.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
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
    private GroupChatExamineMapper groupChatExamineMapper;

    @Resource
   private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertGroupChat(AddGroupChatDTO addGroupChatDTO, UserVO loginUser) {

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
                if (AuthorityConstant.STATUS_BAN.equals(AuthorityConstant.getByAuthority(closeStatic))) {
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
                if(AuthorityConstant.USER_MEMBER.equals(AuthorityConstant.getByAuthority(userRole)) || userService.isAdmin(loginUser)){
                    groupChat.setMaxPeopleNum(120);
                }

                groupChat.setUserId(LoginUserid);
                groupChat.setCurrentPeopleNum(1);
                groupChat.setCreateId(LoginUserid);
                groupChat.setUpdateId(LoginUserid);

                Integer disclosure = addGroupChatDTO.getDisclosure();

                if(disclosure != null){
                    groupChat.setDisclosure(disclosure);
                }


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

    @Override
    public boolean updateGroupChat(UpdateGroupChatDTO updateGroupChatDTO, UserVO loginUser) {
        if(updateGroupChatDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //群聊是否存在
        Long GroupChatId = updateGroupChatDTO.getId();
        GroupChat groupChat = groupChatMapper.selectById(GroupChatId);

        if(groupChat == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //修改对象是否在群里
        Long loginUserId = loginUser.getId();

        UserGroupChatInfo userGroupChatInfo = userGroupChatInfoService.getOne(new QueryWrapper<UserGroupChatInfo>().eq("user_id", loginUserId).eq("group_chat_id", GroupChatId));

        if(userGroupChatInfo == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //修改对象是群聊管理员还是群主
        Integer authority = userGroupChatInfo.getAuthority();

        if(!AuthorityConstant.GROUP_CHAT_ADMIN.equals(AuthorityConstant.getByAuthority(authority)) && !AuthorityConstant.GROUP_CHAT_MASTER.equals(AuthorityConstant.getByAuthority(authority))){
            //如果不是则无权限
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        UpdateWrapper<GroupChat> groupChatUpdateWrapper = new UpdateWrapper<>();
        //可以修改群聊名称、简介、加入状态
        String name = updateGroupChatDTO.getName();
        if(name != null){
            Matcher NameMatcher = Pattern.compile(AccountConstant.groupChat_NAME).matcher(name);
            if(!NameMatcher.find()){
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR,"群聊名称格式错误");
            }
            groupChatUpdateWrapper.set("name",name);
        }

        String groupChatTxt = updateGroupChatDTO.getGroupChatTxt();

        if(groupChatTxt != null){
            groupChatUpdateWrapper.set("group_chat_txt",groupChatTxt);
        }

        Integer disclosure = updateGroupChatDTO.getDisclosure();

        if(disclosure != 1){
            disclosure = 0;
        }

        groupChatUpdateWrapper.set("disclosure",disclosure);

        boolean update = this.update(groupChatUpdateWrapper);

        return update;
    }

    @Override
    public GroupChatExamineVO addExamine(AddGroupChatExamineDTO addGroupChatExamineDTO) {
        if(addGroupChatExamineDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }


        //群聊是否存在
        Long groupChatId = addGroupChatExamineDTO.getGroupChatId();

        GroupChat groupChat = groupChatMapper.selectOne(new QueryWrapper<GroupChat>().eq("id", groupChatId));

        if(groupChat == null){
            //群聊不存在
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //群聊状态是否正常
        Integer status = groupChat.getStatus();

        //如果被封禁
        if(AuthorityConstant.STATUS_BAN.equals(AuthorityConstant.getByAuthority(status))){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //申请人是否正常
        Long userId = addGroupChatExamineDTO.getUserId();


        User user = userService.getById(userId);

        //申请人是否存在
        if(user == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //申请人状态是否正常
        Integer userStatus = user.getCloseStatic();

        if(AuthorityConstant.STATUS_BAN.equals(AuthorityConstant.getByAuthority(userStatus))){
            //如果被封禁
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //申请人是否已经在群聊
        Long id = user.getId();

        long userGroupNum = userGroupChatInfoService.count(new QueryWrapper<UserGroupChatInfo>().eq("user_id", id).eq("group_chat", groupChatId));

        if(userGroupNum > 0){
            //如果存在
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //申请留言最多 150 字
        String examineText = addGroupChatExamineDTO.getExamineText();

        if(examineText != null){
            if(examineText.length() > 150){
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
            }
        }

        //该审核是否已存在
        GroupChatExamine groupChatExamineOne = groupChatExamineMapper.selectOne(new QueryWrapper<GroupChatExamine>().eq("user_id", userId).eq("group_chat_id", groupChatId));


        if(groupChatExamineOne != null){
            return getByGroupExamineVO(groupChatExamineOne);
        }

        //新增审核
        GroupChatExamine groupChatExamine = new GroupChatExamine();

        BeanUtils.copyProperties(addGroupChatExamineDTO,groupChatExamine);

        int insert = groupChatExamineMapper.insert(groupChatExamine);

        if(insert <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        GroupChatExamine save = groupChatExamineMapper.selectById(groupChatExamine.getId());

        return getByGroupExamineVO(save);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupChatExamineVO examineGroupChat(ExamineGroupChatDTO examineGroupChatDTO, UserVO loginUser) {
        if(examineGroupChatDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        Long examineGroupChatId = examineGroupChatDTO.getId();

        //审核数据是否存在
        GroupChatExamine groupChatExamine = groupChatExamineMapper.selectById(examineGroupChatId);
        if(groupChatExamine == null){
            //不存在报异常
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        Long groupChatId = groupChatExamine.getGroupChatId();

        RLock lock = redissonClient.getLock((LockConstant.GROUP_CHAT_LOCK + groupChatId));

        try {
            if (lock.tryLock(0,-1,TimeUnit.SECONDS)) {
                //审核状态是否为未审核
                Integer status = groupChatExamine.getStatus();
                if(!ExamineConstant.EXAMINE_ABSENCE.equals(ExamineConstant.getByStatus(status))){
                    //如果不为未审核
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //审核人是否为本群管理员或群主
                Long loginUserId = loginUser.getId();

                UserGroupChatInfo userGroupChatInfo = userGroupChatInfoService.getOne(new QueryWrapper<UserGroupChatInfo>().eq("group_chat_id", groupChatId).eq("user_id", loginUserId));

                Integer authority = userGroupChatInfo.getAuthority();
                if(!AuthorityConstant.GROUP_CHAT_MASTER.getStatusId().equals(authority) && !AuthorityConstant.GROUP_CHAT_ADMIN.getStatusId().equals(authority)){
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //审核人状态是否正常
                Integer closeStatic = loginUser.getCloseStatic();
                if(AuthorityConstant.STATUS_BAN.getStatusId().equals(closeStatic)){
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }


                //审核人在群里是否被封禁
                Integer userGroupChatInfoStatus = userGroupChatInfo.getStatus();
                if(AuthorityConstant.STATUS_BAN.getStatusId().equals(userGroupChatInfoStatus)){
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //审核结果
                Date createTime = groupChatExamine.getCreateTime();
                long dateTime = createTime.getTime() + 172800000;

                long currentTimeMillis = System.currentTimeMillis();

                UpdateWrapper<GroupChatExamine> updateWrapper = new UpdateWrapper<>();

                //是否过时
                if((dateTime - currentTimeMillis) < 0){
                    //已过时
                    updateWrapper.set("status",ExamineConstant.EXAMINE_OBSOLETE.getCode());
                    updateWrapper.eq("id",examineGroupChatId);
                    int update = groupChatExamineMapper.update(updateWrapper);
                    if(update <= 0){
                        throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                    }
                    return getByGroupExamineVO(groupChatExamine);
                }

                //不同意
                Integer chatDTOStatus = examineGroupChatDTO.getStatus();
                if(ExamineConstant.EXAMINE_DISAGREE.equals(ExamineConstant.getByStatus(chatDTOStatus))){
                    updateWrapper.set("status",ExamineConstant.EXAMINE_DISAGREE.getCode());
                    updateWrapper.eq("id",examineGroupChatId);
                    int update = groupChatExamineMapper.update(updateWrapper);
                    if(update <= 0){
                        throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                    }
                    return getByGroupExamineVO(groupChatExamine);
                }

                //同意
                updateWrapper.set("status",ExamineConstant.EXAMINE_AGREE.getCode());
                updateWrapper.eq("id",examineGroupChatId);
                int update = groupChatExamineMapper.update(updateWrapper);

                if(update <= 0){
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                UserGroupChatInfo userGroupChat = new UserGroupChatInfo();

                Long userId = groupChatExamine.getUserId();
                userGroupChat.setUserId(userId);
                userGroupChat.setGroupChatId(groupChatId);

                //将申请人加入群聊
                boolean save = userGroupChatInfoService.save(userGroupChat);

                if(!save){
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                GroupChat groupChat = groupChatMapper.selectOne(new QueryWrapper<GroupChat>().eq("id", groupChatId));

                Integer groupChatStatus = groupChat.getStatus();

                //如果群聊被封
                if(AuthorityConstant.STATUS_BAN.equals(AuthorityConstant.getByAuthority(groupChatStatus))){
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //当前人数
                Integer currentPeopleNum = groupChat.getCurrentPeopleNum();
                //最大人数
                Integer maxPeopleNum = groupChat.getMaxPeopleNum();

                if(currentPeopleNum >= maxPeopleNum){
                    //当前人数不能大于最大人数
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                int groupChatUpdate = groupChatMapper.update(new UpdateWrapper<GroupChat>().set("current_people_num", currentPeopleNum + 1).eq("id", groupChatId).eq("current_people_num", currentPeopleNum));

                if(groupChatUpdate <= 0){
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }


                return getByGroupExamineVO(groupChatExamine);
            }
        } catch (BusinessException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

        return getByGroupExamineVO(groupChatExamine);
    }


    /**
     * 脱敏审核数据
     * @param groupChatExamine
     * @return
     */
    public GroupChatExamineVO getByGroupExamineVO(GroupChatExamine groupChatExamine){
        if(groupChatExamine == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        GroupChatExamineVO groupChatExamineVO = new GroupChatExamineVO();

        BeanUtils.copyProperties(groupChatExamine,groupChatExamineVO);

        return groupChatExamineVO;
    }
}




