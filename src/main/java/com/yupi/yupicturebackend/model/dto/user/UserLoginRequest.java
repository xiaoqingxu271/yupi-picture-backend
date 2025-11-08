package com.yupi.yupicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
* @author chun0
* @since 2025/11/8 16:51
* @version 1.0
*/
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
}
