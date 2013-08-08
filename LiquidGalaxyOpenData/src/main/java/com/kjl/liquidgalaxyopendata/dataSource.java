package com.kjl.liquidgalaxyopendata;

/**
 * Created by Joan on 7/08/13.
 */
public class dataSource {
    String name;
    String url;
    String filepath;

    public dataSource(String name, String url, String filepath) {
        this.name = name;
        this.url = url;
        this.filepath = filepath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public String toString() {
        return this.getName()+";"+this.getUrl()+";"+this.getFilepath();
    }
}
