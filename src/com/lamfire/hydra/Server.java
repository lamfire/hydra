package com.lamfire.hydra;

import java.net.InetSocketAddress;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.lamfire.logger.Logger;

public class Server extends SessionMgr implements ChannelPipelineFactory{
	private static final Logger LOGGER = Logger.getLogger(Server.class);

    private HydraExecutorMgr executorMgr = HydraExecutorMgr.getInstance();
	private ServerBootstrap bootstrap = null;
	private ChannelFactory channelFactory= null;
	private Channel listenerChannel = null;

	private String bind;
	private int port;
	
	public Server(String bind,int port){
		this.bind = bind;
		this.port = port;
	}
	
	public Server(String bind,int port,MessageHandler handler){
		this(bind, port);
		this.setMessageHandler(handler);
	}
	
	public Server(String bind, int port, SessionEventListener listener) {
		this(bind, port);
		this.setSessionEventListener(listener);
	}

    public void bind(){
		if(listenerChannel != null){
			return ;
		}
		channelFactory = new NioServerSocketChannelFactory(executorMgr.getIoBossExecutor(),executorMgr.getIoWorkerExecutor());
		bootstrap = new ServerBootstrap(channelFactory);
		
		// Set up the default event pipeline.
		bootstrap.setPipelineFactory(this);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		// Bind and start to accept incoming connections.
		listenerChannel = bootstrap.bind(new InetSocketAddress(bind,port));
		LOGGER.debug("Success bind to " +bind +":" +port);
	}

	public void shutdown(){
		super.shutdown();

		if(listenerChannel != null){
			listenerChannel.close();
            listenerChannel = null;
		}
		
		if(channelFactory != null){
            channelFactory.shutdown();
            channelFactory = null;
		}
		
		if(bootstrap != null){
            bootstrap.shutdown();
            bootstrap = null;
		}
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new HydraMessageDecoder());
		pipeline.addLast("encoder", new HydraMessageEncoder());
		pipeline.addLast("handler", this);
		return pipeline;
	}

}
