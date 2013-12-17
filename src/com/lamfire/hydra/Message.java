package com.lamfire.hydra;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Stack;

import com.lamfire.hydra.packet.Packet;
import com.lamfire.hydra.utils.Links;

/**
 * 消息
 * @author admin
 *
 */
public class Message implements Packet<byte []>{
	private final Links links = new Links();
	protected int id = -1;
	protected byte[] bytes;

	public Message(){}
	
	public Message(int id){
		this.id = id;
	}
	
	public Message(byte[] bytes){
		this.bytes = bytes;
	}
	
	public Message(int id,byte[] bytes){
		this.id = id;
		this.bytes = bytes;
	}

	protected synchronized void addLink(int router) {
		links.push(router);
	}
	
	protected synchronized void addAllLinks(List<Integer> list) {
		this.links.addAll(list);
	}

	protected int getLink(int index) {
		return this.links.get(index);
	}
	
	protected Stack<Integer> getLinks() {
		return this.links;
	}

	protected int popLink() {
		return links.pop();
	}

	protected int getRoutersCount() {
		return this.links.size();
	}

	protected boolean isEmptySerial() {
		return this.links.isEmpty();
	}

	/**
	 * 解码消息
	 * @param buf
	 * @return
	 */
	public static Message decodeMessage(ByteBuffer buf) {
		Message msg = new Message();
		msg.decode(buf);
		return msg;
	}
	
	@Override
	public byte[] decode(ByteBuffer buf) {
		buf.flip();
		byte count = buf.get();
		for(int i=0;i<count;i++){
			int serial = buf.getInt();
			links.push(serial);
		}
		this.id = buf.getInt();
		this.bytes = new byte[buf.limit() - 4 - 1 - count * 4];
		buf.get(bytes);
		return this.bytes;
	}

	@Override
	public synchronized ByteBuffer encode() {
		//节点数量
		byte serialCount = (byte)links.size();
		
		//计算节点信息占用数据包长度
		int serialsLength = 0;
		for(byte i=0;i<serialCount;i++){
			serialsLength += 4;
		}
		
		//ID长度
		int idLength = 4;
		
		//数据长度
		int dataLength = 0;
		if(bytes != null){
			dataLength = bytes.length;
		}
		
		//总消息长度 =  串数量长 + 串总长 + ID长 + 数据长
		int totleLength = 1 + serialsLength + idLength + dataLength;
		
		
		ByteBuffer buffer = ByteBuffer.allocate(totleLength);
		
		//添加节点数量
		buffer.put(serialCount);
		
		//添加节点数据
		for(int serial : links){
			buffer.putInt(serial);
		}
		
		//添加消息ID
		buffer.putInt(id);
		
		//添加数据
		if(bytes != null){
			buffer.put(bytes);
		}
		return buffer;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(links.size())
		.append(",").append(links).append(",").append(this.getId()).append(",")
		.append(new String(this.getBody()));
		return buffer.toString();
	}

	@Override
	public byte[] getBody() {
		return bytes;
	}
	
	public String getBodyAsString() {
		return new String(bytes);
	}
	
	public String getBodyAsString(Charset charset) {
		return new String(bytes,charset);
	}

	@Override
	public synchronized void setBody(byte[] e) {
		this.bytes =e;
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
