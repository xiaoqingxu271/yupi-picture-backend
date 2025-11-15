package com.yupi.yupicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author chun0
 * @version 1.0
 * @since 2025/11/11 19:18
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        // 文件不能为空
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 文件大小不能超过2MB
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");
        // 文件格式列表
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!PIC_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式不支持");
    }
}
