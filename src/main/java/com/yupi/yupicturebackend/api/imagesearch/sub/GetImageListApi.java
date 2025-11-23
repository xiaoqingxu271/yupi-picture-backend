package com.yupi.yupicturebackend.api.imagesearch.sub;

import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yupicturebackend.api.imagesearch.model.ImageSearchResult;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
/**
 * @author chun0
 * @since 2025/11/23 16:42
 * @version 1.0
 */
@Slf4j
public class GetImageListApi {

    /**
     * 获取图片列表 （step 3）
     * @param url 图片搜索的url
     * @return 图片列表
     */
    public static List<ImageSearchResult> getImageList(String url) {
        try {
            // 发送请求
            HttpResponse response = HttpUtil.createGet(url).execute();
            // 获取响应的内容
            String body = response.body();
            int statusCode = response.getStatus();
            if (statusCode != 200) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            } else {
                return processResponse(body);
            }
        } catch (Exception e) {
            log.error("获取图片列表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
    }

    private static List<ImageSearchResult> processResponse(String responseBody) {
        // 解析对象
        JSONObject jsonObject = new JSONObject(responseBody);
        if (!jsonObject.containsKey("data")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (!data.containsKey("list")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONArray list = data.getJSONArray("list");
        if (list.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        return JSONUtil.toList(list, ImageSearchResult.class);
    }

    public static void main(String[] args) {
        String url = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1" +
                "&inspire=general_pc&limit=30&next=2&render_type=card&session_id=9860251916875631989&sign=126c11e89be590f7bf3e601763885780&tk=dfeb7&tpl_from=pc";
        List<ImageSearchResult> imageList = getImageList(url);
        System.out.println("搜索成功:" + imageList);
    }
}
