package com.example.meltingbooks.network.group;

import java.io.Serializable;

public class GroupPostResponse implements Serializable {
    private boolean success;
    private GroupResponse data;
    private String error;

    public boolean isSuccess() { return success; }
    public GroupResponse getData() { return data; }
    public String getError() { return error; }

}
