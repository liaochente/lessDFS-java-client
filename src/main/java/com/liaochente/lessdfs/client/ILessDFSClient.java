package com.liaochente.lessdfs.client;

import com.liaochente.lessdfs.DownloadCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public interface ILessDFSClient {

    String upload(byte[] fileBytes, String fileExt) throws FileNotFoundException ;

    String upload(String filePath, String fileExt) throws FileNotFoundException ;

    String upload(Path filePath, String fileExt) throws FileNotFoundException;

    String upload(File file, String fileExt) throws FileNotFoundException;

    String upload(InputStream inputStream, String fileExt);

    <T> T download(String fileName, DownloadCallback<T> callback);

    void delete(String fileName);
}
