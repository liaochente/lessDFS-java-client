package com.liaochente.lessdfs.protcotol;

public enum LessMessageType {
    //    LOGIN_IN("登录认证请求", 98),
//    LOGIN_OUT("登录认证应答", 99),
    INTERNAL_ERROR_OUT("服务器发生内部异常应答", 254),
    UPLOAD_FILE_IN("文件上传请求", 1),
    UPLOAD_FILE_OUT("文件上传应答", 2),
    DOWNLOAD_FILE_IN("文件下载请求", 3),
    DOWNLOAD_FILE_OUT("文件下载应答", 4),
    DELETE_FILE_IN("文件删除请求", 5),
    DELETE_FILE_OUT("文件删除应答", 6),
    HEARTBEAT_IN("心跳请求", 110),
    HEARTBEAT_OUT("心跳应答", 111);

    public final static LessMessageType convert(int type) {
        LessMessageType[] lessMessageTypes = LessMessageType.values();
        for (LessMessageType lessMessageType : lessMessageTypes) {
            if (lessMessageType.type == type) {
                return lessMessageType;
            }
        }
        return null;
    }

    private LessMessageType(String name, int type) {
        this.name = name;
        this.type = type;
    }

    private String name;
    private int type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type + "";
    }
}
