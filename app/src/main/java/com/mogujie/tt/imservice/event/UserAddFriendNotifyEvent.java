package com.mogujie.tt.imservice.event;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 * <p>
 * 添加好友通知
 */
public class UserAddFriendNotifyEvent {
    public long userId;
    public String remark;

    public UserAddFriendNotifyEvent(long id, String remark) {
        userId = id;
        this.remark =remark;
    }

}
