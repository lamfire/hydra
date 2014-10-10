package com.lamfire.hydra;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class HydraMessageDecoder extends FrameDecoder{
	static final int DATA_HEADER_LENGTH = 4;
	
	protected Object decode(ChannelHandlerContext context, Channel channel, ChannelBuffer source) {
		if (source.readableBytes() < DATA_HEADER_LENGTH) {
			return null;
		}
        int readindex = source.readerIndex();
		source.markReaderIndex();
		try {
			int frameLength = source.readInt();
            readindex +=DATA_HEADER_LENGTH;

			//长度为0表示心跳请求
			if(frameLength == 0){
				return HeatbeatType.Request;
			}
			
			//长度为-1表示心跳回复
			if(frameLength == -1){
				return  HeatbeatType.Reply;
			}
			
			//其它表示数据消息
			if (source.readableBytes() < frameLength) {
				source.resetReaderIndex();
				return null;
			}

            //decode
            Message msg = asMessage(source, frameLength);

            readindex += frameLength;
            source.readerIndex(readindex);
			return msg;
		} catch (Exception e) {
			source.resetReaderIndex();
		}
		return null;
	}

    private Message asMessage(ChannelBuffer buf,final int length) {
        Message m = new Message();
        byte links = buf.readByte();
        for(int i=0;i<links;i++){
            int node = buf.readInt();
            m.addLink(node);
        }
        int id = buf.readInt();

        int bodyLength = length - 4 - 1 - links * 4;
        byte[] body = new byte[bodyLength];
        buf.readBytes(body);
        m.setId(id);
        m.setBody(body);
        return m;
    }
	
}
