package com.pengjing.message;

import lombok.Data;
import lombok.ToString;

import java.util.Set;

@Data
@ToString(callSuper = true)
public class GroupCreateRequestMessage extends Message {
    private String groupName;
    private String groupBoss;
    private Set<String> members;

    public GroupCreateRequestMessage(String groupBoss,String groupName, Set<String> members) {
        this.groupName = groupName;
        this.members = members;
        this.groupBoss = groupBoss;
    }

    @Override
    public int getMessageType() {
        return GroupCreateRequestMessage;
    }
}
