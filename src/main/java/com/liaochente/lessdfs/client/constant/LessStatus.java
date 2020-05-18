package com.liaochente.lessdfs.client.constant;

public enum LessStatus {

    INTERNAL_ERROR(205, "服务器内部错误"),
    NOT_FOUND(204, "文件不存在"),
    EMPTY_PROTOCOL(201, "报文为空"),
    FAIL(200, "请求失败"),
    OK(1, "请求成功");

    private LessStatus(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public final static LessStatus convert(int status) {
        LessStatus[] lessStatuses = LessStatus.values();
        for (LessStatus lessStatus : lessStatuses) {
            if(lessStatus.status == status) {
                return lessStatus;
            }
        }
        return null;
    }

    private int status;

    private String message;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
