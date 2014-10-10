package com.lamfire.hydra;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class HydraMessageEncoder extends SimpleChannelHandler{
	static final int DATA_HEADER_LENGTH = 4;
	static final ChannelBuffer HeatbeatReply = ChannelBuffers.buffer(DATA_HEADER_LENGTH);
	static final ChannelBuffer HeatbeatRquest = ChannelBuffers.buffer(DATA_HEADER_LENGTH);
	static{
		HeatbeatRquest.writeInt(0);//心跳请求
		HeatbeatReply.writeInt(-1);//心跳回复
	}
	
	protected  ChannelBuffer encode(ByteBuffer buf){
		buf.flip();
		ChannelBuffer buffer = ChannelBuffers.directBuffer(DATA_HEADER_LENGTH + buf.capacity());
		buffer.writeInt(buf.capacity());
		buffer.writeBytes(buf.array());
		return buffer;
	}
	
	protected  ChannelBuffer encode(byte[] bytes){
		ChannelBuffer buffer = ChannelBuffers.directBuffer(DATA_HEADER_LENGTH + bytes.length);
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
		return buffer;
	}
	
	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e){
		Object msg = e.getMessage();
		if (msg instanceof HeatbeatType) {
			HeatbeatType hb = (HeatbeatType)msg;
			ChannelFuture future = e.getFuture();
			if(HeatbeatType.Reply == hb){
				Channels.write(ctx,future,HeatbeatReply);
				return;
			}
			Channels.write(ctx,future,HeatbeatRquest);
			return;
		}

		if (msg instanceof Message) {
			ChannelFuture future = e.getFuture();
			Channels.write(ctx,future,asChannelBuffer((Message) msg));
			return;
		}
	}

    public synchronized ChannelBuffer asChannelBuffer(Message message) {
        //节点数量
        byte serialCount = (byte)message.getLinksSize();

        //计算节点信息占用数据包长度
        int serialsLength = 0;
        for(byte i=0;i<serialCount;i++){
            serialsLength += 4;
        }

        //ID长度
        int idLength = 4;

        //数据长度
        byte[] bytes = message.getBody();
        int dataLength = 0;
        if(bytes != null){
            dataLength = bytes.length;
        }

        //总消息长度 =  串数量长 + 串总长 + ID长 + 数据长
        int frameLength = 1 + serialsLength + idLength + dataLength;

        ChannelBuffer buffer = ChannelBuffers.directBuffer(DATA_HEADER_LENGTH + frameLength);
        //
        buffer.writeInt(frameLength);
        //添加节点数量
        buffer.writeByte(serialCount);

        //添加节点数据
        for(int serial : message.getLinks()){
            buffer.writeInt(serial);
        }

        //添加消息ID
        buffer.writeInt(message.getId());

        //添加数据
        if(bytes != null){
            buffer.writeBytes(bytes);
        }
        return buffer;
    }
}
