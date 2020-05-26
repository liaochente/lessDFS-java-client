package com.liaochente.lessdfs.client;

import java.io.FileNotFoundException;

public class CommandTool {

    public static void main(String[] args) {
        DefaultLessDFSClient client = DefaultLessDFSClient.newInstance();
        try {
            client.upload(args[0], args[1]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
