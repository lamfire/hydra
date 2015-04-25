package com.lamfire.hydra.utils;

public interface Queue<E> {
	public void push(E e);
	public E pop();
	public int size();
}
