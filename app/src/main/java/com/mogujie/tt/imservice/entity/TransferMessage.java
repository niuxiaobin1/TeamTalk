package com.mogujie.tt.imservice.entity;

import com.mogujie.tt.DB.entity.MessageEntity;
import com.mogujie.tt.DB.entity.PeerEntity;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.config.DBConstant;
import com.mogujie.tt.config.MessageConstant;
import com.mogujie.tt.imservice.support.SequenceNumberMaker;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
public class TransferMessage extends MessageEntity implements Serializable {

     public TransferMessage(){
         msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
     }

     private TransferMessage(MessageEntity entity){
         /**父类的id*/
         id =  entity.getId();
         msgId  = entity.getMsgId();
         fromId = entity.getFromId();
         toId   = entity.getToId();
         sessionKey = entity.getSessionKey();
         content=entity.getContent();
         msgType=entity.getMsgType();
         displayType=entity.getDisplayType();
         status = entity.getStatus();
         created = entity.getCreated();
         updated = entity.getUpdated();
     }

     public static TransferMessage parseFromNet(MessageEntity entity){
         TransferMessage textMessage = new TransferMessage(entity);
         textMessage.setStatus(MessageConstant.MSG_SUCCESS);
         textMessage.setDisplayType(DBConstant.SHOW_PAY_TRANSFER);
         return textMessage;
     }

    public static TransferMessage parseFromDB(MessageEntity entity){
        if(entity.getDisplayType()!=DBConstant.SHOW_PAY_TRANSFER&&
                entity.getDisplayType()!=DBConstant.SHOW_PAY_TRANSFER_OPEN){
            throw new RuntimeException("#TextMessage# parseFromDB,not SHOW_ORIGIN_TEXT_TYPE");
        }
        TransferMessage textMessage = new TransferMessage(entity);
        return textMessage;
    }

    public static TransferMessage buildForSend(String content, UserEntity fromUser, PeerEntity peerEntity,
                                               boolean isOpen){
        TransferMessage textMessage = new TransferMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        textMessage.setFromId(fromUser.getPeerId());
        textMessage.setToId(peerEntity.getPeerId());
        textMessage.setUpdated(nowTime);
        textMessage.setCreated(nowTime);
        textMessage.setDisplayType(isOpen?DBConstant.SHOW_PAY_TRANSFER_OPEN:DBConstant.SHOW_PAY_TRANSFER);
        textMessage.setGIfEmo(true);
        textMessage.setMsgType(isOpen?DBConstant.MSG_TYPE_SINGLE_TRANSFER_OPEN:DBConstant.MSG_TYPE_SINGLE_TRANSFER);
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        textMessage.setContent(content);
        textMessage.buildSessionKey(true);
        return textMessage;
    }


    /**
     * Not-null value.
     * DB的时候需要
     */
    @Override
    public String getContent() {
        return content;
    }

    @Override
    public byte[] getSendContent() {
        try {
            /** 加密*/
            String sendContent =new String(com.mogujie.tt.Security.getInstance().EncryptMsg(content));
            return sendContent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
