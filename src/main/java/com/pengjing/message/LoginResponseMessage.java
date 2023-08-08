package com.pengjing.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class LoginResponseMessage extends AbstractResponseMessage {

    public LoginResponseMessage() {
    }

    public LoginResponseMessage(boolean status, String reason) {
        super(status, reason);
    }

    @Override
    public int getMessageType() {
        return LoginResponseMessage;
    }
}
