package com.liaochente.lessdfs.client;

import java.io.FileNotFoundException;

public class ClientTest {

    public static void main(String[] args) {
        DefaultLessDFSClient client = new DefaultLessDFSClient();
        try {
            String fileName = client.upload("/Users/liaochente/cleanMavenRepository.sh", "sh");
            System.out.println("fileName =" + fileName);
            fileName = client.upload("/Users/liaochente/cleanMavenRepository.sh", "sh");
            System.out.println("fileName =" + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
