package com.mogujie.tt.imservice.event;

import com.mogujie.tt.DB.entity.MessageEntity;
import com.mogujie.tt.imservice.entity.FileMessage;

/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 *
 */
public class FileProgressEvent {
    public int progress=0;
    public FileMessage  fileMessage;
    public FileProgressEvent(int progress,FileMessage  fileMessage){
        this.progress=progress;
        this.fileMessage=fileMessage;
    }

}
