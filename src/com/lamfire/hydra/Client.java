package com.lamfire.hydra;

import java.net.InetSocketAddress;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.lamfire.logger.Logger;

public class Client extends SessionMgr implements ChannelPipelineFactory, Clientable {
	private static final Logger LOGGER = Logger.getLogger(Client.class);
	private ClientBootstrap bootstrap;
	private ChannelFactory channelFactory;
    private HydraExecutorMgr executorMgr = HydraExecutorMgr.getInstance();
	private String host;
	private int port;

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

	public void connect() {
		LOGGER.debug(String.format("Connecting to %s:%d", host, port));
        if(channelFactory == null){
            channelFactory = new NioClientSocketChannelFactory(executorMgr.getIoBossExecutor(), executorMgr.getIoWorkerExecutor());
        }
        if(bootstrap == null){
            bootstrap = new ClientBootstrap(channelFactory);
            bootstrap.setPipelineFactory(this);
        }

		bootstrap.connect(new InetSocketAddress(host, port));
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
        if(channelFactory != null){
            LOGGER.info("[SHUTDOWN] : shutdown channel factory");
            this.channelFactory.shutdown();
            this.channelFactory = null;
        }

        if(bootstrap != null){
            LOGGER.info("[SHUTDOWN] : shutdown client bootstrap");
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
