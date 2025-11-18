package com.yupi.yupicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.yupi.yupicturebackend.config.CosClientConfig;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.manager.CosManager;
import com.yupi.yupicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author chun0
 * @version 1.0
 * @since 2025/11/9 19:01
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosManager cosManager;
    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 1兆
     */
    public final static long ONE_M = 1024 * 1024;

    /**
     * 支持的图片格式列表
     */
    public final static List<String> PIC_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");

    /**
     * 支持的content-type列表
     */
    public final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg");

    /**
     * 上传文件到COS
     *
     * @param inputSource      url | multipartFile
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1, 校验文件
        validPicture(inputSource);
        // 2, 封装图片的上传地址
        String uuid = RandomUtil.randomNumbers(16);
        String originalFilename = getOriginalFilename(inputSource);
        String uploadFileName = String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        File file = null;
        try {
            // 3, 创建临时文件，获取文件到服务器
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源
            processFile(inputSource, file);
            // 4, 上传图片到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 5, 获取图片信息对象, 封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 获取到图片处理的结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                // 获取压缩后的图片信息
                CIObject compressedCiObject = objectList.get(0);
                // 封装压缩后的图片信息
                return buildResult(originalFilename, compressedCiObject);
            }
            return buildResult(imageInfo, uploadPath, originalFilename, file);
        } catch (Exception e) {
            assert file != null;
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 6, 删除临时文件
            assert file != null;
            deleteTempFile(file);
        }
    }

    private UploadPictureResult buildResult(String originalFilename, CIObject compressedCiObject) {
        int PicHeight = compressedCiObject.getHeight();
        int PicWidth = compressedCiObject.getWidth();
        double scale = NumberUtil.round(PicWidth * 1.0 / PicHeight, 2).doubleValue();
        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(PicWidth);
        uploadPictureResult.setPicHeight(PicHeight);
        uploadPictureResult.setPicScale(scale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        return uploadPictureResult;
    }


    /**
     * 处理输入源并且生成本地临时文件的逻辑
     *
     * @param inputSource
     * @param file
     */
    protected abstract void processFile(Object inputSource, File file) throws IOException;

    /**
     * 获取输入源的原始名称
     *
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 校验输入源
     *
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * @param imageInfo
     * @param uploadPath
     * @param originalFilename
     * @param file
     * @return 对象存储返回的图片信息
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, String uploadPath, String originalFilename,
                                            File file) {
        int PicHeight = imageInfo.getHeight();
        int PicWidth = imageInfo.getWidth();
        double scale = NumberUtil.round(PicWidth * 1.0 / PicHeight, 2).doubleValue();
        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(PicWidth);
        uploadPictureResult.setPicHeight(PicHeight);
        uploadPictureResult.setPicScale(scale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }

    /**
     * 删除服务器临时文件
     *
     * @param file
     */
    private static void deleteTempFile(File file) {
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        boolean deleteResult = file.delete();
        ThrowUtils.throwIf(!deleteResult, ErrorCode.SYSTEM_ERROR, "删除临时文件失败");
    }


}
