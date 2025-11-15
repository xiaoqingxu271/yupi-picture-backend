package com.yupi.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
  
    /**  
     * 图片 id（用于修改）  
     */
    private Long id;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 上传图片的地址
     */
    private String fileUrl;
  
    private static final long serialVersionUID = 1L;  
}

