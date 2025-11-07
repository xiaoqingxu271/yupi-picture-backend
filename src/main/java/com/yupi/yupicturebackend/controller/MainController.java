package com.yupi.yupicturebackend.controller;

import com.yupi.yupicturebackend.common.BaseResponse;
import com.yupi.yupicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* @author chun0
* @since 2025/11/7 16:47
* @version 1.0
*/
@RestController
@RequestMapping("/")
public class MainController {
    /**
     * 健康检查
     * @return BaseResponse<String></>
     */
    @GetMapping("health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
