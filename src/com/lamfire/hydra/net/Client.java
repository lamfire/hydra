package com.lamfire.hydra.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.lamfire.logger.Logger;

public class Client extends SessionEventHandler implements ChannelPipelineFactory, Clientable {
	private static final Logger LOGGER = Logger.getLogger(Client.class);
	private ClientBootstrap bootstrap;
	private ChannelFactory channelFactory;
	private String host;
	private int port;
	private boolean shutdowning = false;

	public Client(String host, int port) {
        this.host = host;
        this.port = port;
        channelFactory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        bootstrap = new ClientBootstrap(channelFactory);
        bootstrap.setPipelineFactory(this);
    }

	public Client(String host, int port, SessionEventListener listener) {
		this(host, port);
		this.setSessionEventListener(listener);
	}
	
	public Client(String host,int port,MessageHandler handler){
		this(host, port);
		this.setMessageHandler(handler);
	}

	public synchronized Session connect() {
		if(shutdowning){
			throw new RuntimeException("The client was shutdown,cannot open new connection.");
		}
		LOGGER.debug(String.format("Connecting to %s:%d", host, port));
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
		
		Channel channel = future.getChannel();
		try {
			channel = future.sync().getChannel();
		} catch (InterruptedException e) {
			LOGGER.warn(e.getMessage(),e);
		}
		return new SessionImpl(channel);
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new BytesDecoder());
		pipeline.addLast("encoder", new BytesEncoder());
		pipeline.addLast("handler", this);
		return pipeline;
	}

	public synchronized void shutdown() {
		super.shutdown();
		this.shutdowning = true;
		this.channelFactory.releaseExternalResources();
		this.bootstrap.releaseExternalResources();
		this.channelFactory = null;
		this.bootstrap = null;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		LOGGER.debug("Connected to " + host +":" + port);
	}
}
