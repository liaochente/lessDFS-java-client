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
        InputStream inputStream = client.download("L0/b641807e48ae47d985defebe7c73c145");
        assertNotNull(inputStream);
    }

    @Test
    public void delete() throws Exception {
        DefaultLessDFSClient client = DefaultLessDFSClient.newInstance();
        boolean success = client.delete("L0/b641807e48ae47d985defebe7c73c145");
        assertTrue(success);
    }
}
