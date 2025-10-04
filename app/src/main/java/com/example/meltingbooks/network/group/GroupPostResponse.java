package com.example.meltingbooks.network.group;

import java.io.Serializable;

public class GroupPostResponse implements Serializable {
    private boolean success;
    private GroupProfileResponse data;
    private String error;

    public boolean isSuccess() { return success; }
    public GroupProfileResponse getData() { return data; }
    public String getError() { return error; }

}
