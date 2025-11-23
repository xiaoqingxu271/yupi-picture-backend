package com.yupi.yupicturebackend.api.imagesearch;

import com.yupi.yupicturebackend.api.imagesearch.model.ImageSearchResult;
import com.yupi.yupicturebackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.yupi.yupicturebackend.api.imagesearch.sub.GetImageListApi;
import com.yupi.yupicturebackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @author chun0
* @since 2025/11/23 16:42
* @version 1.0
*/
@Slf4j
public class ImageSearchApiFacade {

    /**
     *搜素图片
     *
     * @param imageUrl 图片地址
     * @return 图片页面地址
     */
    public static List<ImageSearchResult> getImagePageUrl(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        return GetImageListApi.getImageList(imageFirstUrl);
    }

    public static void main(String[] args) {
        String imageUrl = "https://www.codefather.cn/_next/image?url=%2Fimages%2Flogo.png&w=256&q=75";
        List<ImageSearchResult> imagePageUrl = getImagePageUrl(imageUrl);
        System.out.println(imagePageUrl);
    }
}
