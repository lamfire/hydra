package com.lamfire.hydra.net;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class BytesEncoder extends SimpleChannelHandler{
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

		if (msg instanceof ByteBuffer) {
			ChannelFuture future = e.getFuture();
			Channels.write(ctx,future,encode((ByteBuffer) msg));
			return;
		}
		
		if (msg instanceof byte[]) {
			ChannelFuture future = e.getFuture();
			Channels.write(ctx,future,encode((byte[]) msg));
			return;
		}
		
	}
}
