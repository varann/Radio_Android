package org.gigahub.radio.android.api;

import java.util.List;

/**
 * Created by asavinova on 03/10/14.
 */
public class ApiStation {

    private String name;
    private List<ApiStream> streams;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ApiStream> getStreams() {
        return streams;
    }

    public void setStreams(List<ApiStream> streams) {
        this.streams = streams;
    }
}
