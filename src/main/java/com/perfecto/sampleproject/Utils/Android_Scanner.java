package com.perfecto.sampleproject.Utils;

import com.google.gson.annotations.SerializedName;

public class Android_Scanner 
{
	
    private String contentDescription;
    @SerializedName("class")
    private String className;
    private String id;
    private String type;
    private String message;
    private int leftX;
    private int rightX;
    private int topY;
    private int bottomY;
    
    public String getid() {
        return id;
    }
    public void setid(String id) {
        this.id = id;
    }
    public int getleftX() {
        return leftX;
    }
    public void setleftX(int leftX) {
        this.leftX = leftX;
    }
    public int getrightX() {
        return rightX;
    }
    public void setrightX(int rightX) {
        this.rightX = rightX;
    }
    public int gettopY() {
        return topY;
    }
    public void settopY(int topY) {
        this.topY = topY;
    }
    public int getbottomY() {
        return bottomY;
    }
    public void setbottomY(int bottomY) {
        this.bottomY = bottomY;
    }
    public String getcontentDescription() {
        return contentDescription;
    }
    public void setclass(String className) {
        this.className = className;
    }
    public String getclass() {
        return className;
    }
    public void setcontentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }
    public String gettype() {
        return type;
    }
    public void settype(String type) {
        this.type = type;
    }
    public String getmessage() {
        return message;
    }
    public void setmessage(String message) {
        this.message = message;
    }
 
    @Override
    public String toString() {
        return "Android_Scanner [contentDescription=" + contentDescription + ", type=" + type + ", message=" + message + "]";
    }
}
