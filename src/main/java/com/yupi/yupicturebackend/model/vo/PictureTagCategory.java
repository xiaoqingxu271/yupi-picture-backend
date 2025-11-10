package com.yupi.yupicturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @author chun0
* @since 2025/11/10 15:51
* @version 1.0
*/
@Data
public class PictureTagCategory implements Serializable {
    public static final long serialVersionUID = 1L;
    private List<String> tagList;
    private List<String> categoryList;
}
