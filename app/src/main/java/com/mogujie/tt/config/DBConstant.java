package com.mogujie.tt.config;

/**
 * @author : yingmu on 15-1-5.
 * @email : yingmu@mogujie.com.
 */
public interface DBConstant {

    /**
     * 性别
     * 1. 男性 2.女性
     */
    public final int SEX_MAILE = 1;
    public final int SEX_FEMALE = 2;

    /**
     * msgType
     */
    public final int MSG_TYPE_SINGLE_TEXT = 0x01;
    public final int MSG_TYPE_SINGLE_AUDIO = 0x02;
    public final int MSG_TYPE_SINGLE_RED_PACKET = 0x03;
    public final int MSG_TYPE_SINGLE_RED_PACKET_OPEN = 0x04;
    public final int MSG_TYPE_SINGLE_TRANSFER = 0x05;
    public final int MSG_TYPE_SINGLE_TRANSFER_OPEN= 0x06;
    public final int MSG_TYPE_GROUP_TEXT = 0x11;
    public final int MSG_TYPE_GROUP_AUDIO = 0x12;
    public final int MSG_TYPE_GROUP_FILE= 0x14;
    public final int MSG_TYPE_SINGLE_FILE= 0x15;

    /**
     * msgDisplayType
     * 保存在DB中，与服务端一致，图文混排也是一条
     * 1. 最基础的文本信息
     * 2. 纯图片信息
     * 3. 语音
     * 4. 图文混排
     */
    public final int SHOW_ORIGIN_TEXT_TYPE = 1;
    public final int SHOW_IMAGE_TYPE = 2;
    public final int SHOW_AUDIO_TYPE = 3;
    public final int SHOW_MIX_TEXT = 4;
    public final int SHOW_GIF_TYPE = 5;
    public final int SHOW_PAY_RED_PACKET = 6;
    public final int SHOW_PAY_RED_PACKET_OPEN = 7;
    public final int SHOW_PAY_TRANSFER = 8;
    public final int SHOW_PAY_TRANSFER_OPEN = 9;
    public final int SHOW_FILE_TYPE = 10;


    public final String DISPLAY_FOR_RED_PACKET = "[RedPacket]";
    public final String DISPLAY_FOR_RED_PACKET_OPEN = "[RedPacket]";
    public final String DISPLAY_FOR_TRANFER = "[Transfer]";
    public final String DISPLAY_FOR_IMAGE = "[Image]";
    public final String DISPLAY_FOR_MIX = "[Mix]";
    public final String DISPLAY_FILE_DATA = "[File]";
    public final String DISPLAY_FOR_AUDIO = "[Voice]";
    public final String DISPLAY_FOR_ERROR = "[Error]";


    /**
     * sessionType
     */
    public final int SESSION_TYPE_SINGLE = 1;
    public final int SESSION_TYPE_GROUP = 2;
    public final int SESSION_TYPE_ERROR = 3;

    /**
     * user status
     * 1. 试用期 2. 正式 3. 离职 4.实习
     */
    public final int USER_STATUS_PROBATION = 1;
    public final int USER_STATUS_OFFICIAL = 2;
    public final int USER_STATUS_LEAVE = 3;
    public final int USER_STATUS_INTERNSHIP = 4;

    /**
     * group type
     */
    public final int GROUP_TYPE_NORMAL = 1;
    public final int GROUP_TYPE_TEMP = 2;

    /**
     * group status
     * 1: shield  0: not shield
     */

    public final int GROUP_STATUS_ONLINE = 0;
    public final int GROUP_STATUS_SHIELD = 1;

    /**
     * group change Type
     */
    public final int GROUP_MODIFY_TYPE_ADD = 0;
    public final int GROUP_MODIFY_TYPE_DEL = 1;

    /**
     * depart status Type
     */
    public final int DEPT_STATUS_OK = 0;
    public final int DEPT_STATUS_DELETE = 1;

}
