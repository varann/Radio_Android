package org.gigahub.radio.android.api;

import java.util.List;

/**
 * Created by asavinova on 03/10/14.
 */
public class Station {

    private String name;
    private List<Stream> streams;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Stream> getStreams() {
        return streams;
    }

    public void setStreams(List<Stream> streams) {
        this.streams = streams;
    }
}
