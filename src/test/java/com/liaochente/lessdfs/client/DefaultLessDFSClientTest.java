package com.liaochente.lessdfs.client;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class DefaultLessDFSClientTest {

    @Test
    public void upload() throws FileNotFoundException {
        DefaultLessDFSClient client = DefaultLessDFSClient.newInstance();
        String fileName = client.upload("/Users/liaochente/Downloads/头像/v2-4eb23b8d91fa2242405279e4ed9223ec_1440w.jpg", "jpg");
        assertNotNull(fileName);
    }

    @Test
    public void download() throws Exception {
        DefaultLessDFSClient client = DefaultLessDFSClient.newInstance();
        InputStream inputStream = client.download("L0/00/00/97dd15394c2d4bd49b0d14f6f02552d2");
        assertNotNull(inputStream);
    }

    @Test
    public void delete() throws Exception {
        DefaultLessDFSClient client = DefaultLessDFSClient.newInstance();
        boolean success = client.delete("L0/00/01/c9fa2a2c4e0b46e58c7ad19872625ede");
        assertTrue(success);
    }
}
