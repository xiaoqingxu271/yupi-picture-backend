package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.yupi.yupicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.yupi.yupicturebackend.model.entity.SpaceUser;
import com.yupi.yupicturebackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author chun0
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-11-29 14:19:44
 */
public interface SpaceUserService extends IService<SpaceUser> {


    /**
     * 添加空间
     *
     * @param spaceUserAddRequest 空间成员添加请求
     * @return 空间 id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验参数
     *
     * @param spaceUser 空间成员实体对象
     * @param isAdd     是否为添加操作
     */
    void validSpaceUser(SpaceUser spaceUser, boolean isAdd);

    /**
     * 获取单个空间成员脱敏后的数据
     *
     * @param spaceUser 空间成员实体对象
     * @param request   HttpServletRequest
     * @return 空间视图对象
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);


    /**
     * 获取空间成员VO列表
     *
     * @param spaceUserList 空间成员列表
     * @return 空间视图分页对象
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 获取分页查询包装器
     *
     * @param spaceUserQueryRequest 空间成员查询请求
     * @return 查询包装器
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

}
