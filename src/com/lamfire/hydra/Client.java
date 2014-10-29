package com.lamfire.hydra;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
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
    private ExecutorService bossExecutor;
    private ExecutorService workerExecutor;
	private String host;
	private int port;
	private int bossThreads = 2;
    private int workerThreads = 4;

	public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

	public Client(String host, int port, SessionEventListener listener) {
		this(host, port);
		this.setSessionEventListener(listener);
	}
	
	public Client(String host,int port,MessageHandler handler){
		this(host, port);
		this.setMessageHandler(handler);
	}

	public Session connect() {
		LOGGER.debug(String.format("Connecting to %s:%d", host, port));


        if(bossExecutor == null){
            bossExecutor = Executors.newFixedThreadPool(bossThreads);
        }
        if(workerExecutor == null){
            workerExecutor = Executors.newFixedThreadPool(workerThreads);
        }

        if(channelFactory == null){
            channelFactory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);
        }
        if(bootstrap == null){
            bootstrap = new ClientBootstrap(channelFactory);
            bootstrap.setPipelineFactory(this);
        }

		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
		
		Channel channel = future.getChannel();
		try {
			channel = future.sync().getChannel();
		} catch (InterruptedException e) {
			LOGGER.warn(e.getMessage(),e);
		}
		return new SessionImpl(channel);
	}

    public int getBossThreads() {
        return bossThreads;
    }

    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new HydraMessageDecoder());
		pipeline.addLast("encoder", new HydraMessageEncoder());
		pipeline.addLast("handler", this);
		return pipeline;
	}

	public void shutdown() {
		super.shutdown();
        if(this.bossExecutor != null){
            LOGGER.info("[SHUTDOWN] : shutdown boss executor");
            this.bossExecutor.shutdown();;
            this.bossExecutor = null;
        }

        if(this.workerExecutor != null){
            LOGGER.info("[SHUTDOWN] : shutdown worker executor");
            this.workerExecutor.shutdown();
            this.workerExecutor = null;
        }

        if(channelFactory != null){
            LOGGER.info("[SHUTDOWN] : shutdown channel factory");
            this.channelFactory.releaseExternalResources();
            this.channelFactory.shutdown();
            this.channelFactory = null;
        }

        if(bootstrap != null){
            LOGGER.info("[SHUTDOWN] : shutdown client bootstrap");
		    this.bootstrap.releaseExternalResources();
            this.bootstrap.shutdown();
		    this.bootstrap = null;
        }
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		LOGGER.debug("Connected to " + host +":" + port);
	}
}
