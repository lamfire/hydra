package com.lamfire.hydra;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lamfire.logger.Logger;
import com.lamfire.hydra.exception.NotSupportedMethodException;
import com.lamfire.hydra.net.Client;
import com.lamfire.hydra.net.Clientable;
import com.lamfire.hydra.net.Context;
import com.lamfire.hydra.net.HeatbeatType;
import com.lamfire.hydra.net.MessageHandler;
import com.lamfire.hydra.net.Server;
import com.lamfire.hydra.net.Serverable;
import com.lamfire.hydra.net.Session;
import com.lamfire.hydra.net.SessionEventListener;
import com.lamfire.utils.Threads;

/**
 * 网络连接器
 * 可以被动接入,也可以主动接入
 * @author lamfire
 * 
 */
public abstract class Hydra implements MessageHandler,SessionEventListener , Clientable, Serverable {
	static final Logger LOGGER = Logger.getLogger(Hydra.class);
	private Server server;
	private Client client;
	private int keepaliveConnsWithClient = 1;
	private int autoConnectRetryTime=10;
	private String host;
	private int port;
	private boolean isReady = false;
	private HeartbeatTask heartbeatTask;
	private AutoConnectTask autoConnectTask ;
	private int hearbeatIntervalTime = 15;
	private int maxWaitWithHeartbeat = 5;
	private boolean keepAlive = false;
	private boolean autoConnectRetry = false;

	public Hydra(String host, int port) {
		this.host = host;
		this.port = port;
		this.heartbeatTask = new HeartbeatTask(this);
		this.autoConnectTask = new AutoConnectTask(this);
	}

	public int getHearbeatIntervalTime() {
		return hearbeatIntervalTime;
	}

	public int getAutoConnectRetryTime() {
		return autoConnectRetryTime;
	}

	public void setAutoConnectRetryTime(int autoConnectRetryTime) {
		this.autoConnectRetryTime = autoConnectRetryTime;
	}

	public boolean isAutoConnectRetry() {
		return autoConnectRetry;
	}

	public void setAutoConnectRetry(boolean autoConnectRetry) {
		this.autoConnectRetry = autoConnectRetry;
	}

	public int getKeepaliveConnsWithClient() {
		return keepaliveConnsWithClient;
	}

	public void setHearbeatIntervalTime(int hearbeatIntervalTime) {
		if (hearbeatIntervalTime < 1) {
			return;
		}
		this.hearbeatIntervalTime = hearbeatIntervalTime;
	}


	public int getMaxWaitWithHeartbeat() {
		return maxWaitWithHeartbeat;
	}


	public void setMaxWaitWithHeartbeat(int maxWaitWithHeartbeat) {
		if (maxWaitWithHeartbeat < 1) {
			return;
		}
		this.maxWaitWithHeartbeat = maxWaitWithHeartbeat;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	void onReady() {
		if(isReady){
			return;
		}
		if(this.keepAlive){
			this.heartbeatTask.startup();
		}
	}

	public void setKeepaliveConnsWithClient(int conns) {
		if (conns < 1) {
			return;
		}
		this.keepaliveConnsWithClient = conns;
	}

	public boolean hasConnections() {
		Collection<Session> sessions = getSessions();
		if (sessions == null) {
			return false;
		}
		return !sessions.isEmpty();
	}

	boolean isReady() {
		return this.isReady;
	}

	@Override
	public synchronized Session connect() {
		if (client == null) {
			client = new Client(host, port);
			client.setMessageHandler(this);
			client.setSessionEventListener(this);
			if(this.autoConnectRetry){
				this.autoConnectTask.setDelay(autoConnectRetryTime);
				this.autoConnectTask.setKeepaliveConnections(keepaliveConnsWithClient);
				this.autoConnectTask.startup();
			}
		}
		Session session = client.connect();
		for(int i=1;i < keepaliveConnsWithClient ; i++){
			client.connect();
		}
		
		heartbeatTask.setSendHeartbeatRequestEnable(true);
		if (!isReady) {
			try {
				onReady();
				this.isReady = true;
			} catch (Exception e) {
				LOGGER.warn("onReady exception.", e);
			}
		}
		return session;
	}

	@Override
	public void shutdown() {
		if (client != null) {
			client.shutdown();
			client = null;
		}

		if (server != null) {
			server.shutdown();
			server = null;
		}

		autoConnectTask.shutdown();
		heartbeatTask.shutdown();
	}

	@Override
	public synchronized void bind() {
		if(server != null){
			return;
		}
		server = new Server(host, port);
		server.setMessageHandler(this);
		server.setSessionEventListener(this);
		server.bind();
		heartbeatTask.setSendHeartbeatRequestEnable(false);
		try {
			onReady();
			this.isReady = true;
		} catch (Exception e) {
			LOGGER.warn("onReady exception.", e);
		}
	}

	public Session getSession(int sessionId) {
		if (server != null) {
			return server.getSession(sessionId);
		}

		if (client != null) {
			return client.getSession(sessionId);
		}

		return null;
	}

	public Collection<Session> getSessions() {
		if (server != null) {
			return server.getSessions();
		}

		if (client != null) {
			return client.getSessions();
		}
		return null;
	}
	
	public Iterator<Session> getPollerSessionIterator(){
		return new CycleSessionIterator(this);
	}
	
	public final void setSessionEventListener(SessionEventListener listener) {
		throw new NotSupportedMethodException();
	}
	
	@Override
	public void onClosed(Context context, Session session) {
		
	}

	@Override
	public void onConnected(Context context, Session session) {
		
	}

	@Override
	public void onDisconnected(Context context, Session session) {
		
	}

	@Override
	public void onExceptionCaught(Context context, Session session, Throwable throwable) {
        if(LOGGER.isDebugEnabled()){
		    LOGGER.warn("[onExceptionCaught]:"+session.toString(),throwable);
        }
	}

	@Override
	public void onHeatbeat(Context context, Session session, HeatbeatType heatbeat) {
		if(HeatbeatType.Reply.equals(heatbeat)){
			heartbeatTask.onHeartbeat(session);
		}
	}

	@Override
	public void onOpen(Context context, Session session) {
		
	}
}
