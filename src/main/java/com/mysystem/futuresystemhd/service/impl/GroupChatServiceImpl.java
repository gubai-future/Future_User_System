package com.mysystem.futuresystemhd.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysystem.futuresystemhd.common.ErrorCode;
import com.mysystem.futuresystemhd.constant.*;
import com.mysystem.futuresystemhd.domain.DTO.groupChat.*;
import com.mysystem.futuresystemhd.domain.GroupChat;
import com.mysystem.futuresystemhd.domain.GroupChatExamine;
import com.mysystem.futuresystemhd.domain.User;
import com.mysystem.futuresystemhd.domain.UserGroupChatInfo;
import com.mysystem.futuresystemhd.domain.VO.GroupChatExamineVO;
import com.mysystem.futuresystemhd.domain.VO.GroupChatVO;
import com.mysystem.futuresystemhd.domain.VO.UserVO;
import com.mysystem.futuresystemhd.exception.BusinessException;
import com.mysystem.futuresystemhd.mapper.GroupChatExamineMapper;
import com.mysystem.futuresystemhd.service.GroupChatService;
import com.mysystem.futuresystemhd.mapper.GroupChatMapper;
import com.mysystem.futuresystemhd.service.UserGroupChatInfoService;
import com.mysystem.futuresystemhd.service.UserService;
import com.mysystem.futuresystemhd.utils.DateTimeForUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        if (LoginUserid == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        String string = LoginUserid.toString();

        String format = LockConstant.USER_LOCK + string;

        RLock lock = redissonClient.getLock(format);

        try {
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
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
                Long groupChatNum = groupChatMapper.selectCount(new QueryWrapper<GroupChat>().eq("name", name));
                if (groupChatNum >= 1) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR, "群名重复");
                }

                //群聊名正则校验
                Matcher nameMatcher = Pattern.compile(AccountConstant.groupChat_NAME).matcher(name);

                if (!nameMatcher.find()) {
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
                if (AuthorityConstant.USER_MEMBER.equals(AuthorityConstant.getByAuthority(userRole)) || userService.isAdmin(loginUser)) {
                    groupChat.setMaxPeopleNum(120);
                }

                groupChat.setUserId(LoginUserid);
                groupChat.setCurrentPeopleNum(1);
                groupChat.setCreateId(LoginUserid);
                groupChat.setUpdateId(LoginUserid);

                Integer disclosure = addGroupChatDTO.getDisclosure();

                if (disclosure != null) {
                    groupChat.setDisclosure(disclosure);
                }


                boolean groupChatResult = this.save(groupChat);

                long NameCount = this.count(new QueryWrapper<GroupChat>().eq("name", name));

                if (NameCount >= 2 || !groupChatResult) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                UserGroupChatInfo usergroupChatInfo = new UserGroupChatInfo();
                usergroupChatInfo.setUserId(LoginUserid);
                usergroupChatInfo.setGroupChatId(groupChat.getId());
                usergroupChatInfo.setGroupChatName(loginUser.getUserName());
                usergroupChatInfo.setAuthority(2);

                boolean userRanInfoResult = userGroupChatInfoService.save(usergroupChatInfo);

                if (!userRanInfoResult) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                return true;
            }
        } catch (InterruptedException e) {
            log.error("创建队伍失败:", e);
            return false;
        } finally {
            lock.unlock();
        }

        return false;
    }

    @Override
    public boolean updateGroupChat(UpdateGroupChatDTO updateGroupChatDTO, UserVO loginUser) {
        if (updateGroupChatDTO == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //群聊是否存在
        Long GroupChatId = updateGroupChatDTO.getId();
        GroupChat groupChat = groupChatMapper.selectById(GroupChatId);

        if (groupChat == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //修改对象是否在群里
        Long loginUserId = loginUser.getId();

        UserGroupChatInfo userGroupChatInfo = userGroupChatInfoService.getOne(new QueryWrapper<UserGroupChatInfo>().eq("user_id", loginUserId).eq("group_chat_id", GroupChatId));

        if (userGroupChatInfo == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //修改对象是群聊管理员还是群主
        Integer authority = userGroupChatInfo.getAuthority();

        if (!AuthorityConstant.GROUP_CHAT_ADMIN.equals(AuthorityConstant.getByAuthority(authority)) && !AuthorityConstant.GROUP_CHAT_MASTER.equals(AuthorityConstant.getByAuthority(authority))) {
            //如果不是则无权限
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        UpdateWrapper<GroupChat> groupChatUpdateWrapper = new UpdateWrapper<>();
        //可以修改群聊名称、简介、加入状态
        String name = updateGroupChatDTO.getName();
        if (name != null) {
            Matcher NameMatcher = Pattern.compile(AccountConstant.groupChat_NAME).matcher(name);
            if (!NameMatcher.find()) {
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR, "群聊名称格式错误");
            }
            groupChatUpdateWrapper.set("name", name);
        }

        String groupChatTxt = updateGroupChatDTO.getGroupChatTxt();

        if (groupChatTxt != null) {
            groupChatUpdateWrapper.set("group_chat_txt", groupChatTxt);
        }

        Integer disclosure = updateGroupChatDTO.getDisclosure();

        if (disclosure != 1) {
            disclosure = 0;
        }

        groupChatUpdateWrapper.set("disclosure", disclosure);

        return this.update(groupChatUpdateWrapper);
    }

    @Override
    public GroupChatExamineVO addExamine(AddGroupChatExamineDTO addGroupChatExamineDTO) {
        if (addGroupChatExamineDTO == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }


        //群聊是否存在
        Long groupChatId = addGroupChatExamineDTO.getGroupChatId();

        GroupChat groupChat = groupChatMapper.selectOne(new QueryWrapper<GroupChat>().eq("id", groupChatId));

        if (groupChat == null) {
            //群聊不存在
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //群聊状态是否正常
        Integer status = groupChat.getStatus();

        //如果被封禁
        if (AuthorityConstant.STATUS_BAN.equals(AuthorityConstant.getByAuthority(status))) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //申请人是否正常
        Long userId = addGroupChatExamineDTO.getUserId();


        User user = userService.getById(userId);

        //申请人是否存在
        if (user == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //申请人状态是否正常
        Integer userStatus = user.getCloseStatic();

        if (AuthorityConstant.STATUS_BAN.equals(AuthorityConstant.getByAuthority(userStatus))) {
            //如果被封禁
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //申请人是否已经在群聊
        Long id = user.getId();

        long userGroupNum = userGroupChatInfoService.count(new QueryWrapper<UserGroupChatInfo>().eq("user_id", id).eq("group_chat", groupChatId));

        if (userGroupNum > 0) {
            //如果存在
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //申请留言最多 150 字
        String examineText = addGroupChatExamineDTO.getExamineText();

        if (examineText != null) {
            if (examineText.length() > 150) {
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
            }
        }

        //该审核是否已存在
        GroupChatExamine groupChatExamineOne = groupChatExamineMapper.selectOne(new QueryWrapper<GroupChatExamine>().eq("user_id", userId).eq("group_chat_id", groupChatId));


        if (groupChatExamineOne != null) {
            return getByGroupExamineVO(groupChatExamineOne);
        }

        //新增审核
        GroupChatExamine groupChatExamine = new GroupChatExamine();

        BeanUtils.copyProperties(addGroupChatExamineDTO, groupChatExamine);

        int insert = groupChatExamineMapper.insert(groupChatExamine);

        if (insert <= 0) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        GroupChatExamine save = groupChatExamineMapper.selectById(groupChatExamine.getId());

        return getByGroupExamineVO(save);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupChatExamineVO examineGroupChat(ExamineGroupChatDTO examineGroupChatDTO, UserVO loginUser) {
        if (examineGroupChatDTO == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        Long examineGroupChatId = examineGroupChatDTO.getId();

        //审核数据是否存在
        GroupChatExamine groupChatExamine = groupChatExamineMapper.selectById(examineGroupChatId);
        if (groupChatExamine == null) {
            //不存在报异常
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        Long groupChatId = groupChatExamine.getGroupChatId();

        RLock lock = redissonClient.getLock((LockConstant.GROUP_CHAT_LOCK + groupChatId));

        try {
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
                //审核状态是否为未审核
                Integer status = groupChatExamine.getStatus();
                if (!ExamineConstant.EXAMINE_ABSENCE.equals(ExamineConstant.getByStatus(status))) {
                    //如果不为未审核
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //审核人是否为本群管理员或群主
                Long loginUserId = loginUser.getId();

                UserGroupChatInfo userGroupChatInfo = userGroupChatInfoService.getOne(new QueryWrapper<UserGroupChatInfo>().eq("group_chat_id", groupChatId).eq("user_id", loginUserId));

                Integer authority = userGroupChatInfo.getAuthority();
                if (!AuthorityConstant.GROUP_CHAT_MASTER.getStatusId().equals(authority) && !AuthorityConstant.GROUP_CHAT_ADMIN.getStatusId().equals(authority)) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //审核人状态是否正常
                Integer closeStatic = loginUser.getCloseStatic();
                if (AuthorityConstant.STATUS_BAN.getStatusId().equals(closeStatic)) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }


                //审核人在群里是否被封禁
                Integer userGroupChatInfoStatus = userGroupChatInfo.getStatus();
                if (AuthorityConstant.STATUS_BAN.getStatusId().equals(userGroupChatInfoStatus)) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //审核结果
                Date createTime = groupChatExamine.getCreateTime();
                long dateTime = createTime.getTime() + 172800000;

                long currentTimeMillis = System.currentTimeMillis();

                UpdateWrapper<GroupChatExamine> updateWrapper = new UpdateWrapper<>();

                //是否过时
                if ((dateTime - currentTimeMillis) < 0) {
                    //已过时
                    updateWrapper.set("status", ExamineConstant.EXAMINE_OBSOLETE.getCode());
                    updateWrapper.eq("id", examineGroupChatId);
                    int update = groupChatExamineMapper.update(updateWrapper);
                    if (update <= 0) {
                        throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                    }
                    return getByGroupExamineVO(groupChatExamine);
                }

                //不同意
                Integer chatDTOStatus = examineGroupChatDTO.getStatus();
                if (ExamineConstant.EXAMINE_DISAGREE.equals(ExamineConstant.getByStatus(chatDTOStatus))) {
                    updateWrapper.set("status", ExamineConstant.EXAMINE_DISAGREE.getCode());
                    updateWrapper.eq("id", examineGroupChatId);
                    int update = groupChatExamineMapper.update(updateWrapper);
                    if (update <= 0) {
                        throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                    }
                    return getByGroupExamineVO(groupChatExamine);
                }

                //同意
                updateWrapper.set("status", ExamineConstant.EXAMINE_AGREE.getCode());
                updateWrapper.eq("id", examineGroupChatId);
                int update = groupChatExamineMapper.update(updateWrapper);

                if (update <= 0) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                UserGroupChatInfo userGroupChat = new UserGroupChatInfo();

                Long userId = groupChatExamine.getUserId();
                userGroupChat.setUserId(userId);
                userGroupChat.setGroupChatId(groupChatId);

                //将申请人加入群聊
                boolean save = userGroupChatInfoService.save(userGroupChat);

                if (!save) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                GroupChat groupChat = groupChatMapper.selectOne(new QueryWrapper<GroupChat>().eq("id", groupChatId));

                Integer groupChatStatus = groupChat.getStatus();

                //如果群聊被封
                if (AuthorityConstant.STATUS_BAN.equals(AuthorityConstant.getByAuthority(groupChatStatus))) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //当前人数
                Integer currentPeopleNum = groupChat.getCurrentPeopleNum();
                //最大人数
                Integer maxPeopleNum = groupChat.getMaxPeopleNum();

                if (currentPeopleNum >= maxPeopleNum) {
                    //当前人数不能大于最大人数
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                int groupChatUpdate = groupChatMapper.update(new UpdateWrapper<GroupChat>().set("current_people_num", currentPeopleNum + 1).eq("id", groupChatId).eq("current_people_num", currentPeopleNum).gt("max_people_num", maxPeopleNum));

                if (groupChatUpdate <= 0) {
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

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean joinGroupChat(Long groupChatId, UserVO loginUser) {
        if (groupChatId == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //加入的用户是否正常
        Integer closeStatic = loginUser.getCloseStatic();
        if (AuthorityConstant.STATUS_BAN.getStatusId().equals(closeStatic)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        Long userId = loginUser.getId();
        String lockFinal = LockConstant.GROUP_CHAT_JOIN_LOCK + userId + ":" + groupChatId;

        RLock lock = redissonClient.getLock(lockFinal);

        try {
            if (lock.tryLock(0, -1, TimeUnit.SECONDS)) {
                //群聊是否存在
                GroupChat groupChat = groupChatMapper.selectOne(new QueryWrapper<GroupChat>().eq("id", groupChatId));

                if (groupChat == null) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //群聊是否正常
                Integer status = groupChat.getStatus();

                if (AuthorityConstant.STATUS_BAN.getStatusId().equals(status)) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //用户是否已经在群聊
                long userCount = userGroupChatInfoService.count(new QueryWrapper<UserGroupChatInfo>().eq("group_chat_id", groupChatId).eq("user_id", userId));

                if (userCount > 0) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //群聊人数是否异常

                //当前人数
                Integer currentPeopleNum = groupChat.getCurrentPeopleNum();
                //最大人数
                Integer maxPeopleNum = groupChat.getMaxPeopleNum();

                if (currentPeopleNum >= maxPeopleNum) {
                    //当前人数不能大于最大人数
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                //如果群聊状态不为公开
                Integer disclosure = groupChat.getDisclosure();

                if (AuthorityConstant.GROUP_CHAT_PRIVATE.getStatusId().equals(disclosure)) {

                    GroupChatExamine groupChatExamine = new GroupChatExamine();

                    groupChatExamine.setUserId(userId);
                    groupChatExamine.setGroupChatId(groupChatId);

                    int insert = groupChatExamineMapper.insert(groupChatExamine);

                    if (insert <= 0) {
                        throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                    }

                    return false;
                }


                //公开
                int groupChatUpdate = groupChatMapper.update(new UpdateWrapper<GroupChat>().set("current_people_num", currentPeopleNum + 1).eq("id", groupChatId).eq("current_people_num", currentPeopleNum).gt("max_people_num", currentPeopleNum));

                if (groupChatUpdate <= 0) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                UserGroupChatInfo userGroupChatInfo = new UserGroupChatInfo();

                userGroupChatInfo.setUserId(userId);
                userGroupChatInfo.setGroupChatId(groupChatId);

                boolean save = userGroupChatInfoService.save(userGroupChatInfo);

                if (!save) {
                    throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
                }

                return true;

            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

        throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean QuitGroupChat(QuitGroupChatDTO quitGroupChatDTO, UserVO loginUser) {
        if (quitGroupChatDTO == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        //群聊是否存在
        Long groupChatId = quitGroupChatDTO.getGroupChatId();
        GroupChat groupChat = groupChatMapper.selectOne(new QueryWrapper<GroupChat>().eq("id", groupChatId));

        if (groupChat == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //当前对象是否在群聊中
        Long userId = loginUser.getId();
        long UserCount = userGroupChatInfoService.count(new QueryWrapper<UserGroupChatInfo>().eq("user_id", userId).eq("group_chat_id", groupChatId));

        if (UserCount <= 0) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //查看群聊人数如果只要一人删除群聊
        Integer currentPeopleNum = groupChat.getCurrentPeopleNum();
        if (currentPeopleNum <= 1) {
            //删除群聊
            int delete = groupChatMapper.delete(new QueryWrapper<GroupChat>().eq("id", groupChatId).eq("current_people_num", 1));
            if (delete <= 0) {
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
            }
            //删除群聊关系
            boolean userGroupDelete = userGroupChatInfoService.remove(new QueryWrapper<UserGroupChatInfo>().eq("group_chat_id", groupChatId));
            if (!userGroupDelete) {
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
            }
            return true;
        }

        //如果不是最后一人,不是群主直接退出
        Long GroupLeader = groupChat.getUserId();

        if (!userId.equals(GroupLeader)) {
            //不是群主
            boolean userGroupDelete = userGroupChatInfoService.remove(new QueryWrapper<UserGroupChatInfo>().eq("group_chat_id", groupChatId).eq("user_id", userId));
            if (!userGroupDelete) {
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
            }

            int update = groupChatMapper.update(new UpdateWrapper<GroupChat>().eq("id", groupChatId).set("current_people_num", currentPeopleNum - 1).eq("current_people_num", currentPeopleNum));
            if (update <= 0) {
                throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
            }
            return true;
        }

        //如果是群主
        QueryWrapper<UserGroupChatInfo> userGroupChatInfoQueryWrapper = new QueryWrapper<>();
        userGroupChatInfoQueryWrapper.eq("group_chat_id", groupChatId);
        userGroupChatInfoQueryWrapper.eq("authority", AuthorityConstant.GROUP_CHAT_ADMIN.getStatusId());
        userGroupChatInfoQueryWrapper.orderByAsc("create_time");
        userGroupChatInfoQueryWrapper.last("limit 1");

        UserGroupChatInfo userGroupChatInfo = userGroupChatInfoService.getOne(userGroupChatInfoQueryWrapper);

        if (userGroupChatInfo == null) {
            QueryWrapper<UserGroupChatInfo> userGroupChatInfoQueryWrapper1 = new QueryWrapper<>();
            userGroupChatInfoQueryWrapper1.eq("group_chat_id", groupChatId);
            userGroupChatInfoQueryWrapper1.eq("authority", AuthorityConstant.USER_ORDINARY.getStatusId());
            userGroupChatInfoQueryWrapper1.orderByAsc("create_time");
            userGroupChatInfoQueryWrapper1.last("limit 1");
            userGroupChatInfo = userGroupChatInfoService.getOne(userGroupChatInfoQueryWrapper1);
        }

        //将用户变成群主
        Long LeaderId = userGroupChatInfo.getUserId();

        int update = groupChatMapper.update(new UpdateWrapper<GroupChat>().eq("id", groupChatId).set("user_id", LeaderId));
        if (update <= 0) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }
        boolean update1 = userGroupChatInfoService.update(new UpdateWrapper<UserGroupChatInfo>().eq("group_chat_id", groupChatId).eq("user_id", LeaderId).set("authority", AuthorityConstant.GROUP_CHAT_MASTER.getStatusId()));
        if (!update1) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //删除原群主
        boolean remove = userGroupChatInfoService.remove(new QueryWrapper<UserGroupChatInfo>().eq("group_chat_id", groupChatId).eq("user_id", userId).eq("authority", AuthorityConstant.GROUP_CHAT_MASTER.getStatusId()));
        if (!remove) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }


        int userUpdate = groupChatMapper.update(new UpdateWrapper<GroupChat>().eq("id", groupChatId).set("current_people_num", currentPeopleNum - 1).eq("current_people_num", currentPeopleNum));
        if (userUpdate <= 0) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        return true;

    }

    @Override
    public List<GroupChatVO> selectByName(QueryGroupChatByNameDTO queryGroupChatByNameDTO) {
        if (queryGroupChatByNameDTO == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        String name = queryGroupChatByNameDTO.getName();

        Long current = queryGroupChatByNameDTO.getCurrent();

        Long pageSize = queryGroupChatByNameDTO.getPageSize();

        String fieldName = queryGroupChatByNameDTO.getFieldName();

        String sort = queryGroupChatByNameDTO.getSort();

        if (pageSize > 50) {
            pageSize = 50L;
        }

        QueryWrapper<GroupChat> groupChatQueryWrapper = new QueryWrapper<>();

        groupChatQueryWrapper.like("name", name);

        groupChatQueryWrapper.orderBy(StringUtils.isNotBlank(fieldName), SortConstant.SORT_ASC.equals(sort), fieldName);

        Page<GroupChat> groupChatPage = groupChatMapper.selectPage(new Page<>(current, pageSize), groupChatQueryWrapper);

        List<GroupChat> records = groupChatPage.getRecords();

        if (records == null) {
            return null;
        }

        return records.stream().map(this::getByGroupChatVO).collect(Collectors.toList());
    }

    @Override
    public List<GroupChat> selectAdminBy(QueryGroupChatDTO queryGroupChatDTO, UserVO loginUser) {

        if(!userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        QueryWrapper<GroupChat> groupChatQueryWrapper = new QueryWrapper<>();
        queryBy(queryGroupChatDTO, groupChatQueryWrapper);

        Long current = queryGroupChatDTO.getCurrent();
        Long pageSize = queryGroupChatDTO.getPageSize();
        String fieldName = queryGroupChatDTO.getFieldName();
        String sort = queryGroupChatDTO.getSort();

        groupChatQueryWrapper.orderBy(StringUtils.isNotBlank(fieldName),SortConstant.SORT_ASC.equals(sort),fieldName);

        Page<GroupChat> groupChatPage = groupChatMapper.selectPage(new Page<>(current, pageSize), groupChatQueryWrapper);

        List<GroupChat> records = groupChatPage.getRecords();

        return records;

    }

    @Override
    public boolean installGroupChatName(String name, Long groupChatId, UserVO loginUser) {
        if(StringUtils.isBlank(name) || groupChatId == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Long groupNum = groupChatMapper.selectCount(new QueryWrapper<GroupChat>().eq("id", groupChatId));

        if(groupNum <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        Long id = loginUser.getId();

        long userGroupChatNum = userGroupChatInfoService.count(new QueryWrapper<UserGroupChatInfo>().eq("group_chat_id", groupChatId).eq("user_id", id));

        if(userGroupChatNum <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        boolean update = userGroupChatInfoService.update(new UpdateWrapper<UserGroupChatInfo>().set("group_chat_name", name).eq("group_chat_id", groupChatId).eq("user_id", id));

        if(!update){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        return true;
    }

    @Override
    public boolean installGroupChatAthority(InstallGroupChatAuthorityDTO installGroupChatAuthorityDTO, UserVO loginUser) {
        if(installGroupChatAuthorityDTO == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_NULL);
        }

        Long groupChatId = installGroupChatAuthorityDTO.getGroupChatId();

        Long groupChatName = groupChatMapper.selectCount(new QueryWrapper<GroupChat>().eq("id", groupChatId));

        if(groupChatName <= 0){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        Long id = loginUser.getId();

        //登录用户是否是该群聊群主
        long count = userGroupChatInfoService.count(new QueryWrapper<UserGroupChatInfo>().eq("group_chat_id", groupChatId).eq("user_id", id).eq("authority", AuthorityConstant.GROUP_CHAT_MASTER.getStatusId()));

        if(count <= 0){
            throw new BusinessException(ErrorCode.NOT_AUTHORITY);
        }

        Long userId = installGroupChatAuthorityDTO.getUserId();

        //如果操作用户等于修改用户
        if(id.equals(userId)){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        //该用户是否存在群聊
        UserGroupChatInfo userGroupChatInfo = userGroupChatInfoService.getOne(new QueryWrapper<UserGroupChatInfo>().eq("group_chat_id", groupChatId).eq("user_id", userId));

        if(userGroupChatInfo == null){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        Integer authority = installGroupChatAuthorityDTO.getAuthority();

        //如果修改的身份等于该用户的身份
        if(userGroupChatInfo.getAuthority().equals(authority)){
            return true;
        }

        boolean update = userGroupChatInfoService.update(new UpdateWrapper<UserGroupChatInfo>().set("authority", authority).eq("user_id", userId).eq("group_chat_id", groupChatId));

        if(!update){
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        return true;

    }

    private static void queryBy(QueryGroupChatDTO queryGroupChatDTO, QueryWrapper<GroupChat> groupChatQueryWrapper) {
        Long id = queryGroupChatDTO.getId();
        if(id != null){
            groupChatQueryWrapper.eq("id",id);
        }
        String name = queryGroupChatDTO.getName();
        if(StringUtils.isNotBlank(name)){
            groupChatQueryWrapper.like("name",name);
        }
        Long createId = queryGroupChatDTO.getCreateId();
        if(createId != null){
            groupChatQueryWrapper.eq("create_id",createId);
        }
        Integer maxPeopleNum = queryGroupChatDTO.getMaxPeopleNum();
        if(maxPeopleNum != null){
            groupChatQueryWrapper.eq("max_people_num",maxPeopleNum);
        }
        Integer disclosure = queryGroupChatDTO.getDisclosure();
        if(disclosure != null){
            groupChatQueryWrapper.eq("disclosure",disclosure);
        }
        Integer status = queryGroupChatDTO.getStatus();
        if(status != null){
            groupChatQueryWrapper.eq("status",status);
        }
        Integer isDelete = queryGroupChatDTO.getIsDelete();
        if(isDelete != null){
            groupChatQueryWrapper.eq("is_delete",isDelete);
        }
        Long updateId = queryGroupChatDTO.getUpdateId();
        if(updateId != null){
            groupChatQueryWrapper.eq("update_id",updateId);
        }
        Long userId = queryGroupChatDTO.getUserId();
        if(userId != null){
            groupChatQueryWrapper.eq("user_id",userId);
        }
        LocalDateTime createTimeAfter1 = queryGroupChatDTO.getCreateTimeAfter();
        LocalDateTime createTimeBefore1 = queryGroupChatDTO.getCreateTimeBefore();
        if(createTimeAfter1 != null && createTimeBefore1 != null){
            String createTimeAfter = DateTimeForUtils.DateTimeRemoveT(createTimeAfter1);
            String createTimeBefore = DateTimeForUtils.DateTimeRemoveT(createTimeBefore1);
            log.info("createAfter: e",createTimeAfter1);
            groupChatQueryWrapper.last(" AND create_time BETWEEN '" + createTimeBefore + "' AND '" + createTimeAfter + "' ");
        }

        LocalDateTime updateTimeAfter1 = queryGroupChatDTO.getUpdateTimeAfter();
        LocalDateTime updateTimeBefore1 = queryGroupChatDTO.getUpdateTimeBefore();

        if(updateTimeAfter1 != null && updateTimeBefore1 != null){
            String updateTimeAfter = DateTimeForUtils.DateTimeRemoveT(updateTimeAfter1);
            String updateTimeBefore = DateTimeForUtils.DateTimeRemoveT(updateTimeBefore1);
            groupChatQueryWrapper.last(" AND update_time BETWEEN '" + updateTimeBefore + "' AND '" + updateTimeAfter + "' ");
        }
    }


    /**
     * 脱敏审核数据
     * @param groupChatExamine
     * @return
     */
    public GroupChatExamineVO getByGroupExamineVO(GroupChatExamine groupChatExamine) {
        if (groupChatExamine == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        GroupChatExamineVO groupChatExamineVO = new GroupChatExamineVO();

        BeanUtils.copyProperties(groupChatExamine, groupChatExamineVO);

        return groupChatExamineVO;
    }

    /**
     * 脱敏群聊数据
     *
     * @param groupChat
     * @return
     */
    public GroupChatVO getByGroupChatVO(GroupChat groupChat) {
        if (groupChat == null) {
            throw new BusinessException(ErrorCode.REQUEST_IS_ERROR);
        }

        GroupChatVO groupChatVO = new GroupChatVO();

        BeanUtils.copyProperties(groupChat, groupChatVO);

        return groupChatVO;
    }
}




