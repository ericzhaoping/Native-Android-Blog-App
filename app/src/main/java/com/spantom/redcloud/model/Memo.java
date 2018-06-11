package com.spantom.redcloud.model;

import java.util.Date;

public class Memo extends MemoId {

    public String user_id, image_url, title, tagline, description, location, image_thumb;
    public Date timestamp;

    public Memo() {}

    public Memo(String user_id, String image_url, String title, String tagline, String description, String location, String image_thumb, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.title = title;
        this.tagline = tagline;
        this.description = description;
        this.location = location;
        this.image_thumb = image_thumb;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getTitle() {
        return title;
    }

    public String getTagline() {
        return tagline;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


}