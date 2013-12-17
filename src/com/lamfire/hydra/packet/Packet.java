package com.lamfire.hydra.packet;

import java.nio.ByteBuffer;

public interface Packet<E> {
	ByteBuffer encode();
	E decode(ByteBuffer buf);
	E getBody();
	void setBody(E e);
}
