package com.liaochente.lessdfs.client;

import java.io.InputStream;

@FunctionalInterface
public interface DownloadCallback<T> {

    T receive(InputStream inputStream);
}
