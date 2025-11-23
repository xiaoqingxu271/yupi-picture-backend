package com.yupi.yupicturebackend.api.imagesearch.sub;

import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author chun0
 * @since 2025/11/23 16:42
 * @version 1.0
 */
@Slf4j
public class GetImageFirstUrlApi {

    /**
     * 获取图片列表的页面地址 （step 2）
     * @param url 图片搜索的url
     * @return 图片列表的页面地址
     */
    public static String getImageFirstUrl(String url) {
        try {
            // 使用jsoup获取html内容
            Document document = Jsoup.connect(url)
                    .timeout(5000)
                    .get();
            // 获取所有的script标签
            Elements scriptElements = document.getElementsByTag("script");
            // 遍历找到所有包含firstUrl内容的标签
            for (Element scriptElement : scriptElements) {
                String scriptText = scriptElement.html();
                if (scriptText.contains("\"firstUrl\"")) {
                    // 正则表达式提取值
                    Pattern pattern = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
                    Matcher matcher = pattern.matcher(scriptText);
                    if (matcher.find()) {
                        String firstUrl = matcher.group(1);
                        // 处理转义字符
                        firstUrl = firstUrl.replace("\\/", "/");
                        return firstUrl;
                    }
                }
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到url");
        } catch (Exception e) {
            log.error("搜素失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        String url = "https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData[isLogoShow]=1&f=all&isLogoShow=1" +
                "&session_id=9860251916875631989&sign=126c11e89be590f7bf3e601763885780&tpl_from=pc";
        String imageFirstUrl = getImageFirstUrl(url);
        System.out.println(imageFirstUrl);
    }
}
