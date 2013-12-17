package com.lamfire.hydra.net;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class BytesDecoder extends FrameDecoder{
	static final int DATA_HEADER_LENGTH = 4;
	
	protected Object decode(ChannelHandlerContext context, Channel channel, ChannelBuffer source) {
		if (source.readableBytes() < DATA_HEADER_LENGTH) {
			return null;
		}
		source.markReaderIndex();
		try {
			int length = source.readInt();
			
			//长度为0表示心跳请求
			if(length == 0){
				return HeatbeatType.Request;
			}
			
			//长度为-1表示心跳回复
			if(length == -1){
				return  HeatbeatType.Reply;
			}
			
			//其它表示数据消息
			if (source.readableBytes() < length) {
				source.resetReaderIndex();
				return null;
			}
			ByteBuffer buffer = ByteBuffer.allocate(length);
			source.readBytes(buffer);
			return buffer;
		} catch (Exception e) {
			source.resetReaderIndex();
		}
		return null;
	}
	
}
