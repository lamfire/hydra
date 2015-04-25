package com.lamfire.hydra;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.nio.*;

/**
 * Created with IntelliJ IDEA.
 * User: lamfire
 * Date: 14-10-17
 * Time: 下午6:26
 * To change this template use File | Settings | File Templates.
 */
public class MessageBuilder {
    private int id;
    private ChannelBuffer buffer;

    public MessageBuilder(){
        id = 0;
        buffer = ChannelBuffers.dynamicBuffer();
    }

    public MessageBuilder id(int id){
        this.id = id;
        return this;
    }

    public MessageBuilder putInt(int value){
        this.buffer.writeInt(value);
        return this;
    }

    public MessageBuilder put(byte value){
        this.buffer.writeByte(value);
        return this;
    }

    public MessageBuilder putBytes(byte[] value){
        this.buffer.writeBytes(value);
        return this;
    }

    public MessageBuilder putLong(long value){
        this.buffer.writeLong(value);
        return this;
    }

    public MessageBuilder putFloat(float value){
        this.buffer.writeFloat(value);
        return this;
    }

    public Message build(){
        Message message = new Message();
        message.setId(id);
        message.setBody(this.buffer.array());
        this.buffer.clear();
        return message;
    }
}
