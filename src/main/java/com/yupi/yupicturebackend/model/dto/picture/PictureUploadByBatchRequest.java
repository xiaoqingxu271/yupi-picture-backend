package com.yupi.yupicturebackend.model.dto.picture;

import lombok.Data;

@Data
public class PictureUploadByBatchRequest {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 名字前缀
     */
    private String namePrefix;

    /**
     * 抓取数量
     */
    private Integer count = 10;


}

