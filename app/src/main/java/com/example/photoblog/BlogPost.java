package com.example.photoblog;

import java.util.Date;
public class BlogPost {

    public String image_url;
    public String thumbnail;
    public String description;
    public String userId;
    public Date timestamp;


    public BlogPost(){}

    public BlogPost(String image_url, String thumbnail, String description, String userId,Date timestamp) {
        this.image_url = image_url;
        this.thumbnail = thumbnail;
        this.description = description;
        this.userId = userId;
        this.timestamp = timestamp;
    }

   // public Timestamp timestamp;

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}