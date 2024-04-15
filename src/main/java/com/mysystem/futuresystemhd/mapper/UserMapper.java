package com.mysystem.futuresystemhd.mapper;

import com.mysystem.futuresystemhd.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

/**
* @author 古白
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2024-04-01 09:54:39
* @Entity com.mysystem.futuresystemhd.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<User> getByName(@Param("name") String name, @Param("currentPage") Long currentPage, @Param("pageSize") Long pageSize,@Param("fieldName") String fieldName,@Param("sort") String sort);
}




