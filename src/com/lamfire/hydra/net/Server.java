package com.lamfire.hydra.net;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.lamfire.logger.Logger;

public class Server extends SessionEventHandler implements ChannelPipelineFactory{
	private static final Logger LOGGER = Logger.getLogger(Server.class);

    private ExecutorService bossExecutor;
    private ExecutorService workerExecutor;
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

    public void setBossExecutor(ExecutorService bossExecutor) {
        this.bossExecutor = bossExecutor;
    }

    public void setWorkerExecutor(ExecutorService workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    public synchronized void bind(){
		if(listenerChannel != null){
			return ;
		}
        if(bossExecutor == null){
            bossExecutor = Executors.newCachedThreadPool();
        }
        if(workerExecutor == null){
            workerExecutor = Executors.newFixedThreadPool(4);
        }
		channelFactory = new NioServerSocketChannelFactory(bossExecutor,workerExecutor);
		bootstrap = new ServerBootstrap(channelFactory);
		
		// Set up the default event pipeline.
		bootstrap.setPipelineFactory(this);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		// Bind and start to accept incoming connections.
		listenerChannel = bootstrap.bind(new InetSocketAddress(bind,port));
		LOGGER.debug("Success bind to " +bind +":" +port);
	}

	public synchronized void shutdown(){
		super.shutdown();
		
		if(listenerChannel != null){
			listenerChannel.close();
		}
		
		if(channelFactory != null){
			channelFactory.releaseExternalResources();
		}
		
		if(bootstrap != null){
			bootstrap.releaseExternalResources();
		}
		
		listenerChannel = null;
		channelFactory = null;
		bootstrap = null;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new BytesDecoder());
		pipeline.addLast("encoder", new BytesEncoder());
		pipeline.addLast("handler", this);
		return pipeline;
	}

}
