package com.tropo.webapp;

import java.io.Serializable;


public class Bean implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -2948820987377984154L;
    private String url = "http://bean-record-url.com/recording";

    
    public String getUrl() {
    
        return url;
    }

    
    public Bean setUrl(String url) {
    
        this.url = url;
        return this;
    }
    
}
