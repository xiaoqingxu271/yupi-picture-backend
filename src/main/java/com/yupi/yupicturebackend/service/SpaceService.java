package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.yupi.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.yupi.yupicturebackend.model.entity.Space;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author chun0
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-11-22 14:23:07
 */
public interface SpaceService extends IService<Space> {

    /**
     * 校验参数
     *
     * @param space 空间实体对象
     * @param isAdd 是否为添加操作
     */
    void validSpace(Space space, boolean isAdd);


    /**
     * 获取单个空间脱敏后的数据
     *
     * @param space   空间实体对象
     * @param request HttpServletRequest
     * @return 空间视图对象
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);


    /**
     * 获取分页(多个)空间视图对象
     *
     * @param spacePage 空间分页对象
     * @param request   HttpServletRequest
     * @return 空间视图分页对象
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取分页查询包装器
     *
     * @param spaceQueryRequest 空间查询请求
     * @return 查询包装器
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间级别填充空间信息
     * @param space 空间实体对象
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 添加空间
     * @param spaceAddRequest 空间添加请求
     * @param LoginUser       登录用户
     * @return 空间 id
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User LoginUser);

    /**
     * 检查空间权限
     *
     * @param space     空间实体对象
     * @param loginUser 登录用户
     */
    void checkSpaceAuth(Space space, User loginUser);
}
