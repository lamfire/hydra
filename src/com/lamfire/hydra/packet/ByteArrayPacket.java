package com.lamfire.hydra.packet;

import java.nio.ByteBuffer;

public class ByteArrayPacket extends AbstractPacket<byte []>{

	public ByteArrayPacket(){
		
	}
	
	public ByteArrayPacket(byte[] bytes){
		super(bytes);
	}
	
	@Override
	public ByteBuffer encode() {
		byte[] bytes = super.getBody();
		ByteBuffer buf = ByteBuffer.allocate(bytes.length);
		buf.put(bytes);
		buf.flip();
		return buf;
	}

	@Override
	public byte[] decode(ByteBuffer buf) {
		super.setBody(buf.array());
		return super.getBody();
	}

}
