package com.lamfire.hydra.utils;

import java.util.concurrent.LinkedBlockingQueue;

public class FixedQueue<E> implements Queue<E>{
	private final LinkedBlockingQueue<E> queue = new LinkedBlockingQueue<E>();
	private int maxSize = 1024 * 1024;
	
	public FixedQueue(){

	}
	
	public FixedQueue(int maxSize){
		this.maxSize = maxSize;
	}
	
	@Override
	public E pop() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
		}
		return pop();
	}
	
	public void setMaxSize(int maxSize){
		this.maxSize = maxSize;
	}

	@Override
	public void push(E e) {
		if(e == null){
			return;
		}
		try {
			queue.put(e);
		} catch (InterruptedException e1) {
		}
		if(queue.size() > maxSize){
			pop();
		}
	}

	@Override
	public int size() {
		return queue.size();
	}

}
