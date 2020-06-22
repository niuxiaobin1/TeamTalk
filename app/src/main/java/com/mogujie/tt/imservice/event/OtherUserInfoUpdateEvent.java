package com.mogujie.tt.imservice.event;

import com.mogujie.tt.DB.entity.UserEntity;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 *
 * 其他用户信息事件
 * 比如 更改备注 删除
 */
public class OtherUserInfoUpdateEvent {


    public OtherUserInfoUpdateEvent(UpdateEvent updateEvent,UserEntity userEntity){
        this.updateEvent=updateEvent;
        this.userEntity=userEntity;
    }
    public UserEntity userEntity;
    public UpdateEvent updateEvent;
    public enum UpdateEvent{
        USER_UPDATE_INFO_OK,
        USER_DELETE_INFO_OK
    }

}
