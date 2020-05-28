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
        String fileName = client.upload("/Users/liaochente/cleanMavenRepository.sh", "sh");
        assertNotNull(fileName);
    }

    @Test
    public void download() throws Exception {
        DefaultLessDFSClient client = DefaultLessDFSClient.newInstance();
        InputStream inputStream = client.download("L0/00/01/c9fa2a2c4e0b46e58c7ad19872625ede");
        assertNotNull(inputStream);
    }

    @Test
    public void delete() throws Exception {
        DefaultLessDFSClient client = DefaultLessDFSClient.newInstance();
        boolean success = client.delete("L0/00/01/c9fa2a2c4e0b46e58c7ad19872625ede");
        assertTrue(success);
    }
}
