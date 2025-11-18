package com.yupi.yupicturebackend.service.impl;

import java.io.IOException;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.manager.FileManager;
import com.yupi.yupicturebackend.manager.upload.FilePictureUpload;
import com.yupi.yupicturebackend.manager.upload.PictureUploadTemplate;
import com.yupi.yupicturebackend.manager.upload.UrlPictureUpload;
import com.yupi.yupicturebackend.model.dto.file.UploadPictureResult;
import com.yupi.yupicturebackend.model.dto.picture.PictureQueryRequest;
import com.yupi.yupicturebackend.model.dto.picture.PictureReviewRequest;
import com.yupi.yupicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.yupi.yupicturebackend.model.dto.picture.PictureUploadRequest;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.enums.PictureReviewStatusEnum;
import com.yupi.yupicturebackend.model.enums.UserRoleEnum;
import com.yupi.yupicturebackend.model.vo.PictureVO;
import com.yupi.yupicturebackend.service.PictureService;
import com.yupi.yupicturebackend.mapper.PictureMapper;
import com.yupi.yupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;

/**
 * @author chun0
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-11-09 17:42:17
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Resource
    private UserService userService;


    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest,
                                   User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是新增还是更新
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新, 判断图片是否存在
        if (pictureId != null) {
            // 验证图片是否存在
            Picture picture = this.getById(pictureId);
            ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
            // 校验更新的图片是由本人上传或者是管理员
            ThrowUtils.throwIf(!picture.getUserId().equals(loginUser.getId())
                    || !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
//            boolean exists = this.lambdaQuery()
//                    .eq(Picture::getId, pictureId)
//                    .exists();
//            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 上传图片, 得到图片信息
        // 根据id划分库
        String uploadFilePrefix = String.format("public/%s", loginUser.getId());
        // 判断输入流的类型
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadFilePrefix);
        // 构造入库对象
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        String picName = uploadPictureResult.getPicName();
        assert pictureUploadRequest != null;
        if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 操作数据库
        if (pictureId != null) {
            // 如果是更新，需要设置编辑时间和id
            picture.setId(pictureId);
            // 编辑时间
            picture.setEditTime(new Date());
        }
        // 校验审核参数
        this.fillReviewParams(picture, loginUser);
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败 数据库操作失败");
        // 返回vo
        return PictureVO.objToVo(picture);
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = pictureVO.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            pictureVO.setUser(userService.getUserVO(user));
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 转换对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 查询出关联的用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }


    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 取出数据
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();

        // 构建查询条件
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // json数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;

    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null || loginUser == null, ErrorCode.PARAMS_ERROR);
        // 判断图片是否为null
        Long pictureId = pictureReviewRequest.getId();
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        // 判断当前审核状态是否不需要在审核
        Integer reviewStatus =
                PictureReviewStatusEnum.getEnumByValue(pictureReviewRequest.getReviewStatus()).getValue();
        ThrowUtils.throwIf(oldPicture.getReviewStatus().equals(reviewStatus), ErrorCode.PARAMS_ERROR, "图片已审核");
        // 封装参数，更新数据库
        Picture newPicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, newPicture);
        newPicture.setReviewerId(loginUser.getId());
        newPicture.setReviewTime(new Date());
        boolean result = this.updateById(newPicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void fillReviewParams(Picture picture, User user) {
        // 管理员自动过审
        if (userService.isAdmin(user)) {
            picture.setReviewerId(user.getId());
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "一次最多只能上传30张图片");
        // 抓取图片
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Elements div = document.getElementsByClass("dgControl");
        ThrowUtils.throwIf(ObjUtil.isNull(div), ErrorCode.OPERATION_ERROR, "获取元素失败");
        // 解析图片
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)){
                log.error("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            if (StrUtil.isNotBlank(namePrefix)) {
                pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            }
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功 id: {}", pictureVO.getId());
                // 上传数量+1
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }
}




