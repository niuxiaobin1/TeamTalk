package com.mogujie.tt.imservice.event;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 * <p>
 * 用户头像更改信息事件
 */
public class UserAvatarInfoEvent {
    public long userId;
    public String avatar;

    public UserAvatarInfoEvent(long id, String path) {
        userId = id;
        avatar = path;
    }
}
