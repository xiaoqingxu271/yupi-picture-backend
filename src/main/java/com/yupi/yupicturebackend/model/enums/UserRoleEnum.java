package com.yupi.yupicturebackend.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (StrUtil.isBlank(value)) {
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
        Map<String, UserRoleEnum> map = Arrays.stream(UserRoleEnum.values())
                .collect(Collectors.toMap(UserRoleEnum::getValue, userRoleEnum -> userRoleEnum));
        return map.getOrDefault(value, null);

    }
}
