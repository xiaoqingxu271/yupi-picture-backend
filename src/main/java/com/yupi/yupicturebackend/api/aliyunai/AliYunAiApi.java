package com.yupi.yupicturebackend.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yupi.yupicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author chun0
 * @version 1.0
 * @since 2025/11/26 15:23
 */
@Slf4j
@Component
public class AliYunAiApi {
    // 读取配置文件的阿里云的apikey
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 创建任务的地址
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs" +
            ".com/api/v1/services/aigc/image2image/out-painting";

    // 查询任务的地址
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建任务
     *
     * @param createOutPaintingTaskRequest 创建任务请求
     * @return 创建任务响应
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        ThrowUtils.throwIf(createOutPaintingTaskRequest == null, ErrorCode.OPERATION_ERROR, "扩图参数为空");
        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        try (HttpResponse response = httpRequest.execute()) {
             // 解析响应
            if (!response.isOk()) {
                log.error("请求异常: {}", response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }
            CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(response.body(), CreateOutPaintingTaskResponse.class);
            String code = createOutPaintingTaskResponse.getCode();
            if (StrUtil.isNotBlank(code)) {
                String errorMessage = createOutPaintingTaskResponse.getMessage();
                log.error("AI 扩图失败: errCode:{}, errMsg:{}", code, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }
            // 没有异常，返回结果
            return createOutPaintingTaskResponse;
        }
    }

    /**
     * 查询任务
     *
     * @param taskId 任务id
     * @return 查询任务响应
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR, "任务id为空");
        // 发送请求
        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        try (HttpResponse response = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute())
        {
            // 解析响应
            if (!response.isOk()) {
                log.error("请求异常: {}", response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }
            return JSONUtil.toBean(response.body(), GetOutPaintingTaskResponse.class);
        }
    }
}
