package com.yupi.yupicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
/**
 * @author chun0
 * @since 2025/11/23 16:42
 * @version 1.0
 */
@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取图片页面地址 （step 1）
     *
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        // 1. 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        String acsToken = "DuxyTcDbdtT1oyUkHzsqEuUmsyA+KPQy/ydloIDEUOgksRON0yDEwPex1mz0Voyeo" +
                "/E453x4L3r59hQwvXKP1gR9Pu5hXpNNx5PAjWgG92" +
                "/vj8ZDqzY9SFvOZhDQHc93FjapkZlS7D7Ab4CBIxCPHydC3PzcdibyzxO7JwIqwejrvBMCnUbZk6xAKk6wA+M/MD0tThqq" +
                "/j0k8b2OV4bPL/q24i810TKvlqWSNmduh5dNUuhBHKQ3O67DdpUS7X07jaLhKEZocEYw6XP3Ei" +
                "+M3lBwRdf7Evp1UVRz7DGxB7fw9bs8/ojsIntnp3W713/RLjleaBybNeb2L4becjR4tWiPxW98gpD3oEuiwaT2Eyn" +
                "+LOdEh8WdZSko8Jl7yQDdwfzTiiQvbyymUWtMWeBVdoy9/QTGUNb8crLlUFtOXZvwTWSWkr9BiacxZRtii7Yx";
        try {
            // 2. 发送 POST 请求到百度接口
            HttpResponse response = HttpRequest.post(url)
                    .form(formData)
                    .header("Acs-Token", acsToken)
                    .timeout(5000).execute();
            // 判断响应状态
            if (HttpStatus.HTTP_OK != response.getStatus()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            // 解析响应
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);

            // 3. 处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果 URL 为空
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://www.codefather.cn/_next/image?url=%2Fimages%2Flogo.png&w=256&q=75";
        String result = getImagePageUrl(imageUrl);
        System.out.println("搜索成功，结果 URL：" + result);
    }
}

