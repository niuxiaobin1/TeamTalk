package com.mogujie.tt.protobuf.helper;

import com.google.protobuf.ByteString;
import com.mogujie.tt.DB.entity.DepartmentEntity;
import com.mogujie.tt.config.DBConstant;
import com.mogujie.tt.DB.entity.GroupEntity;
import com.mogujie.tt.DB.entity.MessageEntity;
import com.mogujie.tt.DB.entity.SessionEntity;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.config.MessageConstant;
import com.mogujie.tt.imservice.entity.AudioMessage;
import com.mogujie.tt.imservice.entity.MsgAnalyzeEngine;
import com.mogujie.tt.imservice.entity.RedPacketMessage;
import com.mogujie.tt.imservice.entity.TransferMessage;
import com.mogujie.tt.imservice.entity.UnreadEntity;
import com.mogujie.tt.protobuf.IMBaseDefine;
import com.mogujie.tt.protobuf.IMGroup;
import com.mogujie.tt.protobuf.IMMessage;
import com.mogujie.tt.utils.CommonUtil;
import com.mogujie.tt.utils.FileUtil;
import com.mogujie.tt.utils.pinyin.PinYin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import de.greenrobot.dao.test.DbTest;

/**
 * @author : yingmu on 15-1-5.
 * @email : yingmu@mogujie.com.
 *
 */
public class ProtoBuf2JavaBean {

    public static DepartmentEntity getDepartEntity(IMBaseDefine.DepartInfo departInfo){
        DepartmentEntity departmentEntity = new DepartmentEntity();

        int timeNow = (int) (System.currentTimeMillis()/1000);

        departmentEntity.setDepartId(departInfo.getDeptId());
        departmentEntity.setDepartName(departInfo.getDeptName());
        departmentEntity.setPriority(departInfo.getPriority());
        departmentEntity.setStatus(getDepartStatus(departInfo.getDeptStatus()));

        departmentEntity.setCreated(timeNow);
        departmentEntity.setUpdated(timeNow);

        // 设定pinyin 相关
        PinYin.getPinYin(departInfo.getDeptName(), departmentEntity.getPinyinElement());

        return departmentEntity;
    }

    public static UserEntity getUserEntity(IMBaseDefine.UserInfo userInfo){
        UserEntity userEntity = new UserEntity();
        int timeNow = (int) (System.currentTimeMillis()/1000);

        userEntity.setStatus(userInfo.getStatus());
        userEntity.setAvatar(userInfo.getAvatarUrl());
        userEntity.setCreated(timeNow);
        userEntity.setDepartmentId(userInfo.getDepartmentId());
        userEntity.setEmail(userInfo.getEmail());
        userEntity.setGender(userInfo.getUserGender());
        userEntity.setMainName(userInfo.getUserNickName());
        userEntity.setPhone(userInfo.getUserTel());
        userEntity.setPinyinName(userInfo.getUserDomain());
        userEntity.setRealName(userInfo.getUserRealName());
        userEntity.setUpdated(timeNow);
        userEntity.setPeerId(userInfo.getUserId());
        userEntity.setOpenid(userInfo.getOpenid());

        PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
        return userEntity;
    }

    public static SessionEntity getSessionEntity(IMBaseDefine.ContactSessionInfo sessionInfo){
        SessionEntity sessionEntity = new SessionEntity();

        int msgType = getJavaMsgType(sessionInfo.getLatestMsgType());
        sessionEntity.setLatestMsgType(msgType);
        sessionEntity.setPeerType(getJavaSessionType(sessionInfo.getSessionType()));
        sessionEntity.setPeerId(sessionInfo.getSessionId());
        sessionEntity.buildSessionKey();
        sessionEntity.setTalkId(sessionInfo.getLatestMsgFromUserId());
        sessionEntity.setLatestMsgId(sessionInfo.getLatestMsgId());
        sessionEntity.setCreated(sessionInfo.getUpdatedTime());

        String content  = sessionInfo.getLatestMsgData().toStringUtf8();
        String desMessage = new String(com.mogujie.tt.Security.getInstance().DecryptMsg(content));
        // 判断具体的类型是什么
        if(msgType == DBConstant.MSG_TYPE_GROUP_TEXT ||
                msgType ==DBConstant.MSG_TYPE_SINGLE_TEXT){
            desMessage =  MsgAnalyzeEngine.analyzeMessageDisplay(desMessage);
        }else if(msgType== DBConstant.MSG_TYPE_SINGLE_RED_PACKET){
            desMessage=DBConstant.DISPLAY_FOR_RED_PACKET;
        }else if(msgType== DBConstant.MSG_TYPE_SINGLE_RED_PACKET_OPEN){
            desMessage=DBConstant.DISPLAY_FOR_RED_PACKET_OPEN;
        }else if(msgType== DBConstant.MSG_TYPE_SINGLE_TRANSFER){
            desMessage=DBConstant.DISPLAY_FOR_TRANFER;
        }

        sessionEntity.setLatestMsgData(desMessage);
        sessionEntity.setUpdated(sessionInfo.getUpdatedTime());

        return sessionEntity;
    }


    public static GroupEntity getGroupEntity(IMBaseDefine.GroupInfo groupInfo){
        GroupEntity groupEntity = new GroupEntity();
        int timeNow = (int) (System.currentTimeMillis()/1000);
        groupEntity.setUpdated(timeNow);
        groupEntity.setCreated(timeNow);
        groupEntity.setMainName(groupInfo.getGroupName());
        groupEntity.setAvatar(groupInfo.getGroupAvatar());
        groupEntity.setCreatorId(groupInfo.getGroupCreatorId());
        groupEntity.setPeerId(groupInfo.getGroupId());
        groupEntity.setGroupType(getJavaGroupType(groupInfo.getGroupType()));
        groupEntity.setStatus(groupInfo.getShieldStatus());
        groupEntity.setUserCnt(groupInfo.getGroupMemberListCount());
        groupEntity.setVersion(groupInfo.getVersion());
        groupEntity.setlistGroupMemberIds(groupInfo.getGroupMemberListList());

        // may be not good place
        PinYin.getPinYin(groupEntity.getMainName(), groupEntity.getPinyinElement());

        return groupEntity;
    }


    /**
     * 创建群时候的转化
     * @param groupCreateRsp
     * @return
     */
    public static GroupEntity getGroupEntity(IMGroup.IMGroupCreateRsp groupCreateRsp){
        GroupEntity groupEntity = new GroupEntity();
        int timeNow = (int) (System.currentTimeMillis()/1000);
        groupEntity.setMainName(groupCreateRsp.getGroupName());
        groupEntity.setlistGroupMemberIds(groupCreateRsp.getUserIdListList());
        groupEntity.setCreatorId(groupCreateRsp.getUserId());
        groupEntity.setPeerId(groupCreateRsp.getGroupId());

        groupEntity.setUpdated(timeNow);
        groupEntity.setCreated(timeNow);
        groupEntity.setAvatar("");
        groupEntity.setGroupType(DBConstant.GROUP_TYPE_TEMP);
        groupEntity.setStatus(DBConstant.GROUP_STATUS_ONLINE);
        groupEntity.setUserCnt(groupCreateRsp.getUserIdListCount());
        groupEntity.setVersion(1);

        PinYin.getPinYin(groupEntity.getMainName(), groupEntity.getPinyinElement());
        return groupEntity;
    }


    /**
     * 拆分消息在上层做掉 图文混排
     * 在这判断
    */
    public static MessageEntity getMessageEntity(IMBaseDefine.MsgInfo msgInfo) {
        MessageEntity messageEntity = null;
        IMBaseDefine.MsgType msgType = msgInfo.getMsgType();
        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    /**语音的解析不能转自 string再返回来*/
                    messageEntity = analyzeAudio(msgInfo);
                } catch (JSONException e) {
                    return null;
                } catch (UnsupportedEncodingException e) {
                    return null;
                }
                break;

            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_SINGLE_RED_PACK:
                messageEntity = analyzeRedPacket(msgInfo);
                break;
            default:
                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }
        return messageEntity;
    }

    public static MessageEntity analyzeText(IMBaseDefine.MsgInfo msgInfo){
       return MsgAnalyzeEngine.analyzeMessage(msgInfo);
    }

    public static RedPacketMessage analyzeRedPacket(IMBaseDefine.MsgInfo msgInfo) {
        RedPacketMessage redPacketMessage = new RedPacketMessage();
        redPacketMessage.setFromId(msgInfo.getFromSessionId());
        redPacketMessage.setMsgId(msgInfo.getMsgId());
        redPacketMessage.setMsgType(getJavaMsgType(msgInfo.getMsgType()));
        redPacketMessage.setStatus(MessageConstant.MSG_SUCCESS);
        redPacketMessage.setDisplayType(DBConstant.SHOW_PAY_RED_PACKET);
        redPacketMessage.setCreated(msgInfo.getCreateTime());
        redPacketMessage.setUpdated(msgInfo.getCreateTime());

        /**
         * 解密文本信息
         */
        String desMessage = new String(com.mogujie.tt.Security.getInstance().DecryptMsg(msgInfo.getMsgData().toStringUtf8()));
        redPacketMessage.setContent(desMessage);

        return redPacketMessage;
    }

    public static TransferMessage analyzeTransfer(IMBaseDefine.MsgInfo msgInfo) {
        TransferMessage transferMessage = new TransferMessage();
        transferMessage.setFromId(msgInfo.getFromSessionId());
        transferMessage.setMsgId(msgInfo.getMsgId());
        transferMessage.setMsgType(getJavaMsgType(msgInfo.getMsgType()));
        transferMessage.setStatus(MessageConstant.MSG_SUCCESS);
        transferMessage.setDisplayType(DBConstant.SHOW_PAY_TRANSFER);
        transferMessage.setCreated(msgInfo.getCreateTime());
        transferMessage.setUpdated(msgInfo.getCreateTime());

        /**
         * 解密文本信息
         */
        String desMessage = new String(com.mogujie.tt.Security.getInstance().DecryptMsg(msgInfo.getMsgData().toStringUtf8()));
        transferMessage.setContent(desMessage);

        return transferMessage;
    }

    public static RedPacketMessage analyzeOpenRedPacket(IMBaseDefine.MsgInfo msgInfo) {
        RedPacketMessage redPacketMessage = new RedPacketMessage();
        redPacketMessage.setFromId(msgInfo.getFromSessionId());
        redPacketMessage.setMsgId(msgInfo.getMsgId());
        redPacketMessage.setMsgType(getJavaMsgType(msgInfo.getMsgType()));
        redPacketMessage.setStatus(MessageConstant.MSG_SUCCESS);
        redPacketMessage.setDisplayType(DBConstant.SHOW_PAY_RED_PACKET_OPEN);
        redPacketMessage.setCreated(msgInfo.getCreateTime());
        redPacketMessage.setUpdated(msgInfo.getCreateTime());

        /**
         * 解密文本信息
         */
        String desMessage = new String(com.mogujie.tt.Security.getInstance().DecryptMsg(msgInfo.getMsgData().toStringUtf8()));
        redPacketMessage.setContent(desMessage);

        return redPacketMessage;
    }


    public static AudioMessage analyzeAudio(IMBaseDefine.MsgInfo msgInfo) throws JSONException, UnsupportedEncodingException {
        AudioMessage audioMessage = new AudioMessage();
        audioMessage.setFromId(msgInfo.getFromSessionId());
        audioMessage.setMsgId(msgInfo.getMsgId());
        audioMessage.setMsgType(getJavaMsgType(msgInfo.getMsgType()));
        audioMessage.setStatus(MessageConstant.MSG_SUCCESS);
        audioMessage.setReadStatus(MessageConstant.AUDIO_UNREAD);
        audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE);
        audioMessage.setCreated(msgInfo.getCreateTime());
        audioMessage.setUpdated(msgInfo.getCreateTime());

        ByteString bytes = msgInfo.getMsgData();

        byte[] audioStream = bytes.toByteArray();
        if(audioStream.length < 4){
            audioMessage.setReadStatus(MessageConstant.AUDIO_READED);
            audioMessage.setAudioPath("");
            audioMessage.setAudiolength(0);
        }else {
            int msgLen = audioStream.length;
            byte[] playTimeByte = new byte[4];
            byte[] audioContent = new byte[msgLen - 4];

            System.arraycopy(audioStream, 0, playTimeByte, 0, 4);
            System.arraycopy(audioStream, 4, audioContent, 0, msgLen - 4);
            int playTime = CommonUtil.byteArray2int(playTimeByte);
            String audioSavePath = FileUtil.saveAudioResourceToFile(audioContent, audioMessage.getFromId());
            audioMessage.setAudiolength(playTime);
            audioMessage.setAudioPath(audioSavePath);
        }

        /**抽离出来 或者用gson*/
        JSONObject extraContent = new JSONObject();
        extraContent.put("audioPath",audioMessage.getAudioPath());
        extraContent.put("audiolength",audioMessage.getAudiolength());
        extraContent.put("readStatus",audioMessage.getReadStatus());
        String audioContent = extraContent.toString();
        audioMessage.setContent(audioContent);

        return audioMessage;
    }


    public static MessageEntity getMessageEntity(IMMessage.IMMsgData msgData){

        MessageEntity messageEntity = null;
        IMBaseDefine.MsgType msgType = msgData.getMsgType();
        IMBaseDefine.MsgInfo msgInfo = IMBaseDefine.MsgInfo.newBuilder()
                .setMsgData(msgData.getMsgData())
                .setMsgId(msgData.getMsgId())
                .setMsgType(msgType)
                .setCreateTime(msgData.getCreateTime())
                .setFromSessionId(msgData.getFromUserId())
                .build();

        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    messageEntity = analyzeAudio(msgInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                messageEntity = analyzeText(msgInfo);
                break;
            case MSG_TYPE_SINGLE_RED_PACK:
                messageEntity = analyzeRedPacket(msgInfo);
                break;
            case MSG_TYPE_SINGLE_TRANSFER:
                messageEntity = analyzeTransfer(msgInfo);
                break;
            case MSG_TYPE_SINGLE_RECV_RED_PACK:
                messageEntity = analyzeOpenRedPacket(msgInfo);
                break;
            default:
                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }
        if(messageEntity != null){
            messageEntity.setToId(msgData.getToSessionId());
        }

        /**
         消息的发送状态与 展示类型需要在上层做掉
         messageEntity.setStatus();
         messageEntity.setDisplayType();
         */
        return messageEntity;
    }

    public static UnreadEntity getUnreadEntity(IMBaseDefine.UnreadInfo pbInfo){
        UnreadEntity unreadEntity = new UnreadEntity();
        unreadEntity.setSessionType(getJavaSessionType(pbInfo.getSessionType()));
        unreadEntity.setLatestMsgData(pbInfo.getLatestMsgData().toString());
        unreadEntity.setPeerId(pbInfo.getSessionId());
        unreadEntity.setLaststMsgId(pbInfo.getLatestMsgId());
        unreadEntity.setUnReadCnt(pbInfo.getUnreadCnt());
        unreadEntity.buildSessionKey();
        return unreadEntity;
    }

    /**----enum 转化接口--*/
    public static int getJavaMsgType(IMBaseDefine.MsgType msgType){
        switch (msgType){
            case MSG_TYPE_GROUP_TEXT:
                return DBConstant.MSG_TYPE_GROUP_TEXT;
            case MSG_TYPE_GROUP_AUDIO:
                return DBConstant.MSG_TYPE_GROUP_AUDIO;
            case MSG_TYPE_SINGLE_AUDIO:
                return DBConstant.MSG_TYPE_SINGLE_AUDIO;
            case MSG_TYPE_SINGLE_TEXT:
                return DBConstant.MSG_TYPE_SINGLE_TEXT;
            case MSG_TYPE_SINGLE_RED_PACK:
                return DBConstant.MSG_TYPE_SINGLE_RED_PACKET;
            case MSG_TYPE_SINGLE_TRANSFER:
                return DBConstant.MSG_TYPE_SINGLE_TRANSFER;
            case MSG_TYPE_SINGLE_RECV_RED_PACK:
                return DBConstant.MSG_TYPE_SINGLE_RED_PACKET_OPEN;
            default:
                throw new IllegalArgumentException("msgType is illegal,cause by #getProtoMsgType#" +msgType);
        }
    }

    public static int getJavaSessionType(IMBaseDefine.SessionType sessionType){
        switch (sessionType){
            case SESSION_TYPE_SINGLE:
                return DBConstant.SESSION_TYPE_SINGLE;
            case SESSION_TYPE_GROUP:
                return DBConstant.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" +sessionType);
        }
    }

    public static int getJavaGroupType(IMBaseDefine.GroupType groupType){
        switch (groupType){
            case GROUP_TYPE_NORMAL:
                return DBConstant.GROUP_TYPE_NORMAL;
            case GROUP_TYPE_TMP:
                return DBConstant.GROUP_TYPE_TEMP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" +groupType);
        }
    }

    public static int getGroupChangeType(IMBaseDefine.GroupModifyType modifyType){
        switch (modifyType){
            case GROUP_MODIFY_TYPE_ADD:
                return DBConstant.GROUP_MODIFY_TYPE_ADD;
            case GROUP_MODIFY_TYPE_DEL:
                return DBConstant.GROUP_MODIFY_TYPE_DEL;
            default:
                throw new IllegalArgumentException("GroupModifyType is illegal,cause by " +modifyType);
        }
    }

    public static int getDepartStatus(IMBaseDefine.DepartmentStatusType statusType){
        switch (statusType){
            case DEPT_STATUS_OK:
                return DBConstant.DEPT_STATUS_OK;
            case DEPT_STATUS_DELETE:
                return DBConstant.DEPT_STATUS_DELETE;
            default:
                throw new IllegalArgumentException("getDepartStatus is illegal,cause by " +statusType);
        }

    }
}
