package com.liaochente.lessdfs;

import java.io.InputStream;

@FunctionalInterface
public interface DownloadCallback<T> {

    T receive(InputStream inputStream);
}
