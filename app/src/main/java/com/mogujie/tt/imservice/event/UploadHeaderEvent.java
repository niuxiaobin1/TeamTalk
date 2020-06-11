package com.mogujie.tt.imservice.event;

import com.mogujie.tt.DB.entity.MessageEntity;

import java.util.ArrayList;

/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 *
 */
public class UploadHeaderEvent {

    private String  url;
    private Event event;

    public UploadHeaderEvent(){
    }

    public UploadHeaderEvent(Event event){
        //默认值 初始化使用
        this.event = event;
    }

    public UploadHeaderEvent(Event event, String url){
        //默认值 初始化使用
        this.event = event;
        this.url=url;
    }

    public enum Event{
        HEADER_IMAGE_UPLOAD_FAILD,
        HEADER_IMAGE_UPLOAD_SUCCESS
     }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
