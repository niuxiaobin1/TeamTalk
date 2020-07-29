package com.mogujie.tt.imservice.manager;

import android.util.Log;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessageLite;
import com.loopj.android.http.AsyncHttpClient;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imservice.callback.ListenerQueue;
import com.mogujie.tt.imservice.callback.Packetlistener;
import com.mogujie.tt.imservice.entity.FileMessage;
import com.mogujie.tt.imservice.event.SocketEvent;
import com.mogujie.tt.imservice.network.FileReceiveServerHandler;
import com.mogujie.tt.imservice.network.FileServerHandler;
import com.mogujie.tt.imservice.network.SocketThread;
import com.mogujie.tt.protobuf.IMBaseDefine;
import com.mogujie.tt.protobuf.IMFile;
import com.mogujie.tt.protobuf.base.DataBuffer;
import com.mogujie.tt.protobuf.base.DefaultHeader;
import com.mogujie.tt.utils.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;

import de.greenrobot.event.EventBus;


/**
 * @author : yingmu on 14-12-30.
 * @email : yingmu@mogujie.com.
 * <p>
 * 业务层面:接收文件
 * 长连接建立成功之后，就要发送登陆信息，否则15s之内就会断开
 * 所以connMsg 与 login是强耦合的关系
 */
public class IMFileReceiveSocketManager extends IMManager {
    private Logger logger = Logger.getLogger(IMFileReceiveSocketManager.class);
    private static IMFileReceiveSocketManager inst = new IMFileReceiveSocketManager();

    public static IMFileReceiveSocketManager instance() {
        return inst;
    }

    public IMFileReceiveSocketManager() {
        logger.d("login#creating IMSocketManager");
    }

    private ListenerQueue listenerQueue = ListenerQueue.instance();

    // 请求消息服务器地址
    private AsyncHttpClient client = new AsyncHttpClient();

    /**
     * 底层socket
     */
    private SocketThread fileServerThread;

    /**
     * 快速重新连接的时候需要
     */
    private FileServerAddrsEntity currentFileAddress = null;

    /**
     * 自身状态
     */
    private SocketEvent socketStatus = SocketEvent.NONE;

    /**
     * 获取Msg地址，等待链接
     */
    @Override
    public void doOnStart() {
        socketStatus = SocketEvent.NONE;
    }

    private FileMessage mReceiveFileMessage = null;

    //todo check
    @Override
    public void reset() {
        disconnectFileServer();
        socketStatus = SocketEvent.NONE;
        currentFileAddress = null;
    }

    /**
     * 实现自身的事件驱动
     *
     * @param event
     */
    public void triggerEvent(SocketEvent event) {
        setSocketStatus(event);
        EventBus.getDefault().postSticky(event);
    }

    /**
     * -------------------------------功能方法--------------------------------------
     */

    public void sendRequest(GeneratedMessageLite requset, int sid, int cid) {
        sendRequest(requset, sid, cid, null);
    }


    /**
     * todo check exception
     */
    public void sendRequest(GeneratedMessageLite requset, int sid, int cid, Packetlistener packetlistener) {
        int seqNo = 0;
        try {
            //组装包头 header
            com.mogujie.tt.protobuf.base.Header header = new DefaultHeader(sid, cid);
            int bodySize = requset.getSerializedSize();
            header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + bodySize);
            seqNo = header.getSeqnum();
            listenerQueue.push(seqNo, packetlistener);
            boolean sendRes = fileServerThread.sendRequest(requset, header);
        } catch (Exception e) {
            if (packetlistener != null) {
                packetlistener.onFaild();
            }
            listenerQueue.pop(seqNo);
            logger.e("#file sendRequest#channel is close!");
        }
    }

    public void packetDispatch(ChannelBuffer channelBuffer) {
        DataBuffer buffer = new DataBuffer(channelBuffer);
        com.mogujie.tt.protobuf.base.Header header = new com.mogujie.tt.protobuf.base.Header();
        header.decode(buffer);
        /**buffer 的指针位于body的地方*/
        int commandId = header.getCommandId();
        int serviceId = header.getServiceId();
        int seqNo = header.getSeqnum();
        logger.d("file server dispatch packet, serviceId:%d, commandId:%d", serviceId,
                commandId);
        CodedInputStream codedInputStream = CodedInputStream.newInstance(new ChannelBufferInputStream(buffer.getOrignalBuffer()));

        Packetlistener listener = listenerQueue.pop(seqNo);
        if (listener != null) {
            listener.onSuccess(codedInputStream);
            return;
        }

        switch (serviceId) {
            case IMBaseDefine.ServiceID.SID_FILE_VALUE:
                IMPacketDispatcher.filePacketDispatcher(commandId, codedInputStream);
                break;
            default:
                logger.e("file packet#unhandled serviceId:%d, commandId:%d", serviceId,
                        commandId);
                break;
        }
    }

    public void reqFileServer(IMFile.IMFileNotify imFileNotify) {
        if (imFileNotify.getIpAddrListCount() != 0) {
            FileServerAddrsEntity addrsEntity = new FileServerAddrsEntity();
            addrsEntity.priorIP = imFileNotify.getIpAddrList(0).getIp();
            addrsEntity.port = imFileNotify.getIpAddrList(0).getPort();
            connectFileServer(addrsEntity);
        }
    }

    public void reqFileServer(FileMessage fileMessage) {
        if (fileMessage != null) {
            mReceiveFileMessage = fileMessage;
            FileServerAddrsEntity addrsEntity = new FileServerAddrsEntity();
            addrsEntity.priorIP = fileMessage.getIp();
            addrsEntity.port = fileMessage.getPort();
            connectFileServer(addrsEntity);
        }
    }


    /**
     * 与登陆login是强耦合的关系
     */
    private void connectFileServer(FileServerAddrsEntity currentFileAddress) {
        triggerEvent(SocketEvent.CONNECT_FILE_SERVER_FAILED);
        this.currentFileAddress = currentFileAddress;

        String priorIP = currentFileAddress.priorIP;
        int port = currentFileAddress.port;
        logger.i("login#connectFileServer -> (%s:%d)", priorIP, port);

        //check again,may be unimportance
        if (fileServerThread != null) {
            fileServerThread.close();
            fileServerThread = null;
        }

        fileServerThread = new SocketThread(priorIP, port, new FileReceiveServerHandler());
        fileServerThread.start();
    }

    public void reconnectFile() {
        synchronized (IMFileReceiveSocketManager.class) {
            if (currentFileAddress != null) {
                connectFileServer(currentFileAddress);
            } else {
                disconnectFileServer();
            }
        }
    }

    /**
     * 断开与msg的链接
     */
    public void disconnectFileServer() {
        listenerQueue.onDestory();
        logger.i("login#disconnectFileServer");
        if (fileServerThread != null) {
            fileServerThread.close();
            fileServerThread = null;
            logger.i("login#do real disconnectFileServer ok");
        }
    }

    /**
     * 判断链接是否处于断开状态
     */
    public boolean isSocketConnect() {
        if (fileServerThread == null || fileServerThread.isClose()) {
            return false;
        }
        return true;
    }

    public void onFileServerConnected() {
        logger.i("login#onFileServerConnected");
        listenerQueue.onStart();
        triggerEvent(SocketEvent.CONNECT_FILE_SERVER_SUCCESS);
        if (mReceiveFileMessage != null) {
            IMMessageManager.instance().loginFileReceiveServer(mReceiveFileMessage);
        }

    }

    /**
     * 1. kickout 被踢出会触发这个状态   -- 不需要重连
     * 2. 心跳包没有收到 会触发这个状态   -- 链接断开，重连
     * 3. 链接主动断开                 -- 重连
     * 之前的长连接状态 connected
     */
    // 先断开链接
    // only 2 threads(ui thread, network thread) would request sending  packet
    // let the ui thread to close the connection
    // so if the ui thread has a sending task, no synchronization issue
    public void onFileServerDisconn() {
        logger.w("login#onMsgServerDisconn");
        disconnectFileServer();
        triggerEvent(SocketEvent.FILE_SERVER_DISCONNECTED);
    }

    /**
     * 之前没有连接成功
     */
    public void onConnectFileServerFail() {
        triggerEvent(SocketEvent.CONNECT_FILE_SERVER_FAILED);
    }


    /**----------------------------请求Msg server地址--实体信息--------------------------------------*/
    /**
     * 请求返回的数据
     */
    private class FileServerAddrsEntity {
        int code;
        String msg;
        String priorIP;
        String backupIP;
        int port;

        @Override
        public String toString() {
            return "LoginServerAddrsEntity{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    ", priorIP='" + priorIP + '\'' +
                    ", backupIP='" + backupIP + '\'' +
                    ", port=" + port +
                    '}';
        }
    }


    /**
     * ------------get/set----------------------------
     */
    public SocketEvent getSocketStatus() {
        return socketStatus;
    }

    public void setSocketStatus(SocketEvent socketStatus) {
        this.socketStatus = socketStatus;
    }


}
