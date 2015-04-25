package com.lamfire.hydra;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.lamfire.hydra.exception.HydraException;
import com.lamfire.logger.Logger;
import com.lamfire.hydra.exception.NotSupportedMethodException;
import com.lamfire.hydra.Client;
import com.lamfire.hydra.Clientable;
import com.lamfire.hydra.Context;
import com.lamfire.hydra.HeatbeatType;
import com.lamfire.hydra.MessageHandler;
import com.lamfire.hydra.Server;
import com.lamfire.hydra.Serverable;
import com.lamfire.hydra.Session;
import com.lamfire.hydra.SessionEventListener;
import com.lamfire.utils.Threads;

/**
 * 网络连接器
 * 可以被动接入,也可以主动接入
 * @author lamfire
 * 
 */
public abstract class Hydra implements MessageHandler,SessionEventListener , Clientable, Serverable {
	static final Logger LOGGER = Logger.getLogger(Hydra.class);
    private final static AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    private final Lock lock = new ReentrantLock();
    private final int id;
    private final String host;
    private final int port;
	private Server server;
	private Client client;
	private int keepaliveConnsWithClient = 1;
	private int autoConnectRetryTime=10;
	private boolean isReady = false;
	private HeartbeatTask heartbeatTask;
	private int hearbeatIntervalTime = 15;
	private int maxWaitWithHeartbeat = 5;
	private boolean keepAlive = false;
	private boolean autoConnectRetry = false;
    private CycleSessionIterator cycleSessionIterator;

	public Hydra(String host, int port) {
		this.host = host;
		this.port = port;
        this.id = INSTANCE_COUNTER.incrementAndGet();
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

	public int getPort() {
		return port;
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
	public void connect() {
        lock.lock();
        try{
            if(this.heartbeatTask == null){
                this.heartbeatTask = new HeartbeatTask(this);
            }

            if (client == null) {
                client = new Client(host, port);
                client.setMessageHandler(this);
                client.setSessionEventListener(this);
                if(this.autoConnectRetry){
                    AutoConnectTask task = AutoConnectTask.getInstance();
                    task.setDelay(autoConnectRetryTime);
                    task.add(this);
                    task.startup();
                }
            }
            client.connect();
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
        }finally {
            lock.unlock();
        }
	}

	@Override
	public void shutdown() {
        lock.lock();
        try{
            if (client != null) {
                LOGGER.info("[SHUTDOWN] : Client");
                client.shutdown();
                client = null;
            }

            if (server != null) {
                LOGGER.info("[SHUTDOWN] : Server");
                server.shutdown();
                server = null;
            }

            if(autoConnectRetry){
                LOGGER.info("[SHUTDOWN] : AtuoConnectTask");
                AutoConnectTask.getInstance().remove(this);
            }

            if(heartbeatTask != null){
                LOGGER.info("[SHUTDOWN] : HeartbeatTask" );
                heartbeatTask.shutdown();
                heartbeatTask = null;
            }
        }finally {
            lock.unlock();
        }
	}

	@Override
	public void bind() {
        lock.lock();
        try{
            if(server != null){
                return;
            }
            if(server == null){
                server = new Server(host, port);
                server.setMessageHandler(this);
                server.setSessionEventListener(this);
            }

            server.bind();

            if(this.heartbeatTask == null){
                this.heartbeatTask = new HeartbeatTask(this);
            }
            heartbeatTask.setSendHeartbeatRequestEnable(false);
            try {
                onReady();
                this.isReady = true;
            } catch (Exception e) {
                LOGGER.warn("onReady exception.", e);
            }
        }finally {
            lock.unlock();
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
	
	public CycleSessionIterator getSessionIterator(){
        lock.lock();
        try{
            if(cycleSessionIterator != null){
                return cycleSessionIterator;
            }

            if (server != null) {
                cycleSessionIterator = new CycleSessionIterator(server);
            }

            if (client != null) {
                cycleSessionIterator = new CycleSessionIterator(client);
            }
            return cycleSessionIterator;
        }finally {
            lock.unlock();
        }
	}

    public Session awaitAvailableSession(){
        if(client != null){
            return client.awaitAvailableSession();
        }
        if(server != null){
            return server.awaitAvailableSession();
        }
        throw new HydraException("Hydra not bootstrap");
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
		    LOGGER.warn("[EXCEPTION]:"+session.toString(),throwable);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hydra hydra = (Hydra) o;

        if (id != hydra.id) return false;
        if (port != hydra.port) return false;
        if (host != null ? !host.equals(hydra.host) : hydra.host != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "Hydra{"  + host + ":" + port +"#" + id + "}";
    }
}
