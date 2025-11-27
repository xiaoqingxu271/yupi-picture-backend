package com.yupi.yupicturebackend.model.dto.space.analyse;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    /**
     * 排名前 N 的空间
     */
    private Integer topN = 10;

    @Serial
    private static final long serialVersionUID = 1L;
}

