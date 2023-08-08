package com.pengjing.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public abstract class AbstractResponseMessage extends Message {
    private boolean status;
    private String reason;

    public AbstractResponseMessage() {
    }

    public AbstractResponseMessage(boolean status, String reason) {
        this.status = status;
        this.reason = reason;
    }
}
