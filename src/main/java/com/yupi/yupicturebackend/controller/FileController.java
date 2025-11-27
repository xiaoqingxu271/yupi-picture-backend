package com.yupi.yupicturebackend.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.yupi.yupicturebackend.annotation.AuthCheck;
import com.yupi.yupicturebackend.common.BaseResponse;
import com.yupi.yupicturebackend.common.ResultUtils;
import com.yupi.yupicturebackend.constant.UserConstant;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * @author chun0
 * @version 1.0
 * @since 2025/11/9 17:57
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 测试上传文件
     * @param multipartFile 文件
     * @return 文件路径
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", fileName);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath, file);
            return ResultUtils.success(filePath);
        } catch (Exception e) {
            log.error("file upload error filePath: {}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "file upload error");
        } finally {
            // 删除临时文件
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error filePath: {}", filePath);
                }
            }
        }
    }

    /**
     * 测试下载文件
     * @param key 文件路径
     * @param response 响应
     */
    @GetMapping("/test/download")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void testDownloadFile(String key, HttpServletResponse response) {
        COSObjectInputStream objectContent = null;
        try {
            COSObject cosManagerObject = cosManager.getObject(key);
            objectContent = cosManagerObject.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(objectContent);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + key);
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error key: {}", key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "file download error");
        } finally {
            if (objectContent != null) {
                try {
                    objectContent.close();
                } catch (Exception e) {
                    log.error("file close error key: {}", key, e);
                }
            }
        }
    }
}
