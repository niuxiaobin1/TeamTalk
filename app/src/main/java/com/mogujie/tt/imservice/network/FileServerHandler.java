package com.mogujie.tt.imservice.network;

import com.mogujie.tt.imservice.manager.IMFileSocketManager;
import com.mogujie.tt.imservice.manager.IMHeartBeatManager;
import com.mogujie.tt.imservice.manager.IMSocketManager;
import com.mogujie.tt.protobuf.IMFile;
import com.mogujie.tt.utils.Logger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class FileServerHandler extends SimpleChannelHandler {

    private Logger logger = Logger.getLogger(FileServerHandler.class);

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelConnected(ctx, e);
        logger.d("channel#channelConnected");
        IMFileSocketManager.instance().onFileServerConnected();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
  		super.channelDisconnected(ctx, e);
        IMFileSocketManager.instance().onFileServerDisconn();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		super.messageReceived(ctx, e);
        // 重置AlarmManager的时间
        ChannelBuffer channelBuffer = (ChannelBuffer) e.getMessage();
        if(null!=channelBuffer)
            IMFileSocketManager.instance().packetDispatch(channelBuffer);
	}

    /**
     * bug问题点:
     * exceptionCaught会调用断开链接， channelDisconnected 也会调用断开链接，事件通知冗余不合理。
     * a.另外exceptionCaught 之后channelDisconnected 依旧会被调用。 [切花网络方式]
     * b.关闭channel 也可能触发exceptionCaught
     * recvfrom failed: ETIMEDOUT (Connection timed out) 没有关闭长连接
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        super.exceptionCaught(ctx, e);
        if(e.getChannel() == null || !e.getChannel().isConnected()){
            IMFileSocketManager.instance().onConnectFileServerFail();
        }
        logger.e("channel#[网络异常了]exceptionCaught:%s", e.getCause().toString());
    }
}
