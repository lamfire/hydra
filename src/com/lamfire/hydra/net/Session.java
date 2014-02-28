package com.lamfire.hydra.net;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;

import org.jboss.netty.channel.Channel;

import com.lamfire.hydra.packet.Packet;
/**
 * 会话接口
 * 用于标识一个连接,及获取该连接的属性
 * @author admin
 *
 */
public interface Session{
	public int getSessionId();
	public Future send(byte[] bytes);
	public Future send(ByteBuffer buffer);
	public Future send(Packet<?> packet);
	public Future sendHeatbeatRequest();
	public Future sendHeatbeatReply();
	public SocketAddress getRemoteAddress();
	public SocketAddress getLocalAddress();
	public Channel getChannel();
	public Object getAttribute(String key);
	public void setAttribute(String key,Object value);
	public void clearAttributes();
	public Map<String,Object> getAttributes();
	public Future close();
	public boolean isClosed();
	public boolean isConnected();
	public boolean isSendable();
    public long getSendCount();
    public long getSendCompletedCount();
    public long getSendBufferedSize();
    public void setSessionClosedListener(SessionClosedListener sessionClosedListener);
    public SessionClosedListener getSessionClosedListener();
}
