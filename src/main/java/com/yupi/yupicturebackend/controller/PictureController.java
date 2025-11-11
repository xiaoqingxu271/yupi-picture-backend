package com.yupi.yupicturebackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupicturebackend.annotation.AuthCheck;
import com.yupi.yupicturebackend.common.BaseResponse;
import com.yupi.yupicturebackend.common.DeleteRequest;
import com.yupi.yupicturebackend.common.ResultUtils;
import com.yupi.yupicturebackend.constant.UserConstant;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.model.dto.file.UploadPictureResult;
import com.yupi.yupicturebackend.model.dto.picture.PictureEditRequest;
import com.yupi.yupicturebackend.model.dto.picture.PictureQueryRequest;
import com.yupi.yupicturebackend.model.dto.picture.PictureUpdateRequest;
import com.yupi.yupicturebackend.model.dto.picture.PictureUploadRequest;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.PictureTagCategory;
import com.yupi.yupicturebackend.model.vo.PictureVO;
import com.yupi.yupicturebackend.service.PictureService;
import com.yupi.yupicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author chun0
 * @version 1.0
 * @since 2025/11/7 16:47
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;

    /**
     * 上传图片
     * @param multipartFile 上传的文件
     * @param pictureUploadRequest 上传请求参数
     * @param request HttpServletRequest
     * @return 图片VO
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest
    , HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断当前图片是否是本人创建的图片或者当前用户是管理员
        User user = userService.getLoginUser(request);
        Long pictureId = deleteRequest.getId();
        Picture oldPicture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        if (!oldPicture.getUserId().equals(user.getId()) && !user.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = pictureService.removeById(pictureId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 更新图片信息 （仅管理员可以使用）
     * @param pictureUpdateRequest 更新请求参数
     * @return 更新是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 实体类与DTO进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureService, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断更新的图片是否存在
        Picture oldPicture = pictureService.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片 （仅管理员可用）
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 设置User
        User user = userService.getById(picture.getUserId());
        return ResultUtils.success(picture);
    }

    /**
     * 根据id获取用户vo信息， （这是给普通用户的）
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 设置User
        User user = userService.getById(picture.getUserId());
        // 转换成VO返回
        PictureVO pictureVO = PictureVO.objToVo(picture);
        pictureVO.setUser(userService.getUserVO(user));
        return ResultUtils.success(pictureVO);
    }

    /**
     * 分页查询图片列表（仅管理员可以调用）
     * @param pictureQueryRequest 图片分页查询的请求体
     * @return Page<Picture>
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片vo, 给普通用户使用
     * @param pictureQueryRequest 图片分页查询的请求体
     * @return Page<PictureVO></>
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest
    , HttpServletRequest request) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();
        // 限制大小，防止被爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 转换成VO实体
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 编辑图片信息，给用户自己使用
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 实体转换为DTO
        Picture picture = new Picture();
        // 属性拷贝
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 转换tags
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 校验数据
        pictureService.validPicture(picture);
        // 判断当前编辑的图片是否存在
        Picture oldPicture = pictureService.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 判断当前编辑图片的用户是否是当前图片的上传者或者是管理员
        User user = userService.getLoginUser(request);
        if (!oldPicture.getUserId().equals(user.getId()) && !user.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);

    }

    /**
     * 模拟一些标签预热展示
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }




}
