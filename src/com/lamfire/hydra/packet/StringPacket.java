package com.lamfire.hydra.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class StringPacket extends AbstractPacket<String>{

	private String charset;
	
	public StringPacket(){}
	
	public StringPacket(String data){
		super(data);
	}
	
	public StringPacket(String data,String charset){
		super(data);
		this.charset = charset;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public ByteBuffer encode() {
		String data = getBody();
		if(data == null){
			return null;
		}
		byte[] bytes = null;
		if(charset != null){
			try {
				bytes = data.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}else{
			bytes = data.getBytes();
		}

		ByteBuffer buf = ByteBuffer.allocate(bytes.length);
		buf.put(bytes);
		buf.flip();
		return buf;
	}

	@Override
	public String decode(ByteBuffer buf) {
		setBody(new String(buf.array()));
		return getBody();
	}


}
