package com.lamfire.hydra.packet;

public abstract class AbstractPacket<E> implements Packet<E>{

	private E e;
	
	public AbstractPacket(){}
	
	public AbstractPacket(E e){
		this.e = e;
	}
	
	public void setBody(E e) {
		this.e = e;
	}
	
	public E getBody(){
		return e;
	}
}
