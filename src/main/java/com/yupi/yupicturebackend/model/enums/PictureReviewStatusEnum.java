package com.yupi.yupicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum PictureReviewStatusEnum {

    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);



    private final String text;

    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static PictureReviewStatusEnum getEnumByValue(int value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        // 这里可以优化-使用map存储枚举值和枚举对象的映射关系，避免遍历
        /*for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;*/
        // 优化后的代码
        Map<Integer, PictureReviewStatusEnum> map = Arrays.stream(PictureReviewStatusEnum.values())
                .collect(Collectors.toMap(PictureReviewStatusEnum::getValue, pictureReviewStatusEnum -> pictureReviewStatusEnum));
        return map.getOrDefault(value, null);

    }
}
