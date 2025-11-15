package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupicturebackend.model.dto.picture.PictureQueryRequest;
import com.yupi.yupicturebackend.model.dto.picture.PictureReviewRequest;
import com.yupi.yupicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.yupi.yupicturebackend.model.dto.picture.PictureUploadRequest;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author chun0
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-11-09 17:42:17
*/
public interface PictureService extends IService<Picture> {


    /**
     * 上传图片
     *
     * @param inputSource 上传的文件
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser 当前登录用户
     * @return 图片视图对象
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    /**
     * 获取单个图片脱敏后的数据
     * @param picture 图片实体对象
     * @param request HttpServletRequest
     * @return 图片视图对象
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);


    /**
     * 获取分页(多个)图片视图对象
     * @param picturePage 图片分页对象
     * @param request HttpServletRequest
     * @return 图片视图分页对象
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验参数
     * @param picture 图片实体对象
     */
    void validPicture(Picture picture);

    /**
     * 获取分页查询包装器
     * @param pictureQueryRequest 图片查询请求
     * @return 查询包装器
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 处理图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            当前登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数，方便其他方法使用
     *
     * @param picture 图片实体对象
     * @param user    当前登录用户
     */
    void fillReviewParams(Picture picture, User user);

    /**
     * 批量上传图片
     *
     * @param pictureUploadByBatchRequest 批量上传图片请求
     * @param loginUser                   当前登录用户
     * @return 返回上传图片的数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);
}
