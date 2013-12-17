package com.lamfire.hydra.net;

import java.util.Collection;

import org.jboss.netty.channel.Channel;

public interface Context {

	public Session getSession(int sessionId);
	
	public Session getSession(Channel channel);
	
	public Collection<Session> getSessions();
	
}
