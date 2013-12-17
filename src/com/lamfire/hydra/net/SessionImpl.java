package com.lamfire.hydra.net;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

import com.lamfire.logger.Logger;
import com.lamfire.hydra.packet.Packet;
import com.lamfire.utils.Maps;

public class SessionImpl implements Session, Comparable<Session> {
	static final Logger LOGGER = Logger.getLogger(SessionImpl.class);
	private Map<String, Object> attributes;
	private Channel channel;

	protected SessionImpl(Channel channel) {
		this.channel = channel;
	}

	public int getSessionId() {
		return this.channel.getId();
	}

	protected Future sendDatas(Object datas) {
		if (!this.channel.isConnected()) {
			throw new SessionException("The channel was closed,cannot write message.");
		}
		ChannelFuture future = Channels.write(channel, datas);
		return new Future(this, future);
	}

	public Future send(byte[] bytes) {
		return sendDatas(bytes);
	}

	public Future send(ByteBuffer buffer) {
		return sendDatas(buffer.array());
	}

	public Future send(Packet<?> packet) {
		return sendDatas(packet.encode().array());
	}

	public SocketAddress getRemoteAddress() {
		return this.channel.getRemoteAddress();
	}

	public SocketAddress getLocalAddress() {
		return this.channel.getLocalAddress();
	}

	public Channel getChannel() {
		return this.channel;
	}

	public Object getAttribute(String key) {
		if (this.attributes == null) {
			return null;
		}
		return this.attributes.get(key);
	}

	public void setAttribute(String key, Object value) {
		if (this.attributes == null) {
			this.attributes = Maps.newHashMap();
		}
		this.attributes.put(key, value);
	}

	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	public void clearAttributes() {
		if (this.attributes != null) {
			this.attributes.clear();
			this.attributes = null;
		}
	}

	public Future close() {
		ChannelFuture future = this.channel.close();
		return new Future(this, future);
	}

	public boolean isClosed() {
		return !channel.isOpen();
	}

	public boolean isConnected() {
		return channel.isConnected();
	}

	public boolean isSendable() {
		return channel.isWritable();
	}

	public String toString() {
		try {
			String s = this.getRemoteAddress().toString() + "/" + this.getSessionId();
			return s;
		} catch (Exception e) {
			return super.toString();
		}
	}

	@Override
	public int compareTo(Session o) {
		if (this.channel.getId() > o.getChannel().getId()) {
			return 1;
		}
		if (this.channel.getId() < o.getChannel().getId()) {
			return -1;
		}
		return 0;
	}

	@Override
	public Future sendHeatbeatRequest() {
		ChannelFuture future = Channels.write(channel, HeatbeatType.Request);
		return new Future(this, future);
	}

	@Override
	public Future sendHeatbeatReply() {
		ChannelFuture future = Channels.write(channel, HeatbeatType.Reply);
		return new Future(this, future);
	}
}
