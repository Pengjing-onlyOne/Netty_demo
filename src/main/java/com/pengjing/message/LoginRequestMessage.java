package com.pengjing.message;

import lombok.Data;
import lombok.ToString;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * 登录使用的对象
 */
@Data
@ToString(callSuper = true)
public class LoginRequestMessage extends Message {
    private String username;
    private String password;

    public LoginRequestMessage() {
    }

    public LoginRequestMessage(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public  int getMessageType() {
        return LoginRequestMessage;
    }
}
