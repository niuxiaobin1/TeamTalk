package com.mogujie.tt.imservice.entity;

import android.os.Environment;

import com.luck.picture.lib.entity.LocalMedia;
import com.mogujie.tt.DB.entity.MessageEntity;
import com.mogujie.tt.DB.entity.PeerEntity;
import com.mogujie.tt.DB.entity.UserEntity;
import com.mogujie.tt.app.IMApplication;
import com.mogujie.tt.config.DBConstant;
import com.mogujie.tt.config.MessageConstant;
import com.mogujie.tt.imservice.manager.IMLoginManager;
import com.mogujie.tt.imservice.service.IMService;
import com.mogujie.tt.imservice.support.SequenceNumberMaker;
import com.mogujie.tt.protobuf.IMLogin;
import com.mogujie.tt.ui.adapter.album.ImageItem;
import com.mogujie.tt.utils.AppContext;
import com.mogujie.tt.utils.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
public class FileMessage extends MessageEntity implements Serializable {

    /**
     * 本地保存的path
     */
    private String path = "";
    private long size = 0;
    private String taskId = "";
    private String ip = "";
    private int port = 0;

    private int loadStatus;


    public FileMessage() {
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
    }

    /**
     * 消息拆分的时候需要
     */
    private FileMessage(MessageEntity entity) {
        /**父类的id*/
        id = entity.getId();
        msgId = entity.getMsgId();
        fromId = entity.getFromId();
        toId = entity.getToId();
        sessionKey = entity.getSessionKey();
        content = entity.getContent();
        msgType = entity.getMsgType();
        displayType = entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
    }


    public static FileMessage parseFromDB(MessageEntity entity) {
        if (entity.getDisplayType() != DBConstant.SHOW_FILE_TYPE) {
            throw new RuntimeException("#ImageMessage# parseFromDB,not SHOW_FILE_TYPE");
        }
        FileMessage fileMessage = new FileMessage(entity);
        String originContent = entity.getContent();
        JSONObject extraContent;
        try {
            extraContent = new JSONObject(originContent);
            fileMessage.setPath(extraContent.getString("path"));
            fileMessage.setSize(extraContent.getLong("size"));
            fileMessage.setTaskId(extraContent.getString("taskId"));
            fileMessage.setIp(extraContent.getString("ip"));
            fileMessage.setPort(extraContent.getInt("port"));

            int loadStatus = extraContent.getInt("loadStatus");

            //todo temp solution
            if (loadStatus == MessageConstant.IFILE_LOADING) {
                loadStatus = MessageConstant.FILE_UNLOAD;
            }
            fileMessage.setLoadStatus(loadStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return fileMessage;
    }

    // 消息页面，发送文件消息
    public static FileMessage buildForSend(String path, UserEntity fromUser, PeerEntity peerEntity) throws IOException {
        FileMessage msg = new FileMessage();
        if (new File(path).exists()) {
            msg.setPath(path);
            msg.setSize(CommonUtil.getFileSize(new File(path)));
        }
        // 将文件发送至服务器
        int nowTime = (int) (System.currentTimeMillis() / 1000);


        msg.setFromId(fromUser.getPeerId());
        msg.setToId(peerEntity.getPeerId());
        msg.setCreated(nowTime);
        msg.setUpdated(nowTime);
        msg.setDisplayType(DBConstant.SHOW_FILE_TYPE);
        // content 自动生成的
        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_FILE :
                DBConstant.MSG_TYPE_SINGLE_FILE;
        msg.setMsgType(msgType);

        msg.setStatus(MessageConstant.MSG_SENDING);
        msg.setLoadStatus(MessageConstant.FILE_UNLOAD);
        msg.buildSessionKey(true);
        return msg;
    }

    // 消息页面，接收文件消息
    public static FileMessage buildForSend(String fileName, int fromId,int size) throws IOException {
        FileMessage msg = new FileMessage();
        String receivePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            receivePath = IMApplication.sApplicationContext.getExternalCacheDir().getPath();
        } else {
            receivePath = IMApplication.sApplicationContext.getFilesDir().getPath();
        }

        msg.setPath(receivePath+File.separator+fileName);
        if (new File(msg.getPath()).exists()){
            msg.setPath(receivePath+File.separator+System.currentTimeMillis()+fileName);
        }
        if (!new File(msg.getPath()).exists()){
            new File(msg.getPath()).createNewFile();
        }
        msg.setSize(size);
        // 将文件发送至服务器
        int nowTime = (int) (System.currentTimeMillis() / 1000);


        msg.setFromId(fromId);
        msg.setToId(IMLoginManager.instance().getLoginId());
        msg.setCreated(nowTime);
        msg.setUpdated(nowTime);
        msg.setDisplayType(DBConstant.SHOW_FILE_TYPE);
        // content 自动生成的
        int msgType = DBConstant.MSG_TYPE_SINGLE_FILE;
        msg.setMsgType(msgType);

        msg.setStatus(MessageConstant.MSG_SENDING);
        msg.setLoadStatus(MessageConstant.FILE_UNLOAD);
        msg.buildSessionKey(true);
        return msg;
    }


    /**
     * Not-null value.
     */
    @Override
    public String getContent() {
        JSONObject extraContent = new JSONObject();
        try {
            extraContent.put("path", path);
            extraContent.put("loadStatus", loadStatus);
            extraContent.put("taskId", taskId);
            extraContent.put("ip", ip);
            extraContent.put("port", port);
            extraContent.put("size", size);
            String fileContent = extraContent.toString();
            return fileContent;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] getSendContent() {

        return null;
    }

    /**
     * -----------------------set/get------------------------
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
