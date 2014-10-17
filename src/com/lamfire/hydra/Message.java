package com.lamfire.hydra;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Stack;

import com.lamfire.hydra.utils.Links;

/**
 * 消息
 * @author admin
 *
 */
public final class Message{
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
        if(list == null || list.isEmpty()){
            return;
        }
		this.links.addAll(list);
	}

    protected void clearLinks(){
        this.links.clear();
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

	protected int getLinksSize() {
		return this.links.size();
	}

	protected boolean isEmptyLinks() {
		return this.links.isEmpty();
	}

	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(links.size())
		.append(",").append(links).append(",").append(this.getId()).append(",")
		.append(new String(this.getBody()));
		return buffer.toString();
	}

	public byte[] getBody() {
		return bytes;
	}
	
	public String getBodyAsString() {
		return new String(bytes);
	}
	
	public String getBodyAsString(Charset charset) {
		return new String(bytes,charset);
	}

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
