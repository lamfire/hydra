package com.lamfire.hydra;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.lamfire.logger.Logger;
import com.lamfire.hydra.net.Session;

/**
 * 心跳检测任务
 */
class HeartbeatTask extends HydraTask {
	private static final Logger LOGGER = Logger.getLogger(HeartbeatTask.class);
	private Hydra hydra;
	private final Map<Integer,Long> heatbeatTimeMap = new HashMap<Integer,Long>();//记录Session收到的心跳时间，用于对比Session是否断开。
	private int heartbeatSeconds = 60;
	private int maxWaitCount = 5;
	private boolean sendHeartbeatRequestEnable = true;

	
	public HeartbeatTask(Hydra hydra){
		super("HEARTBEAT");
		this.hydra = hydra;
	}

	public boolean isSendHeartbeatRequestEnable() {
		return sendHeartbeatRequestEnable;
	}



	public void setSendHeartbeatRequestEnable(boolean sendHeartbeatRequestEnable) {
		this.sendHeartbeatRequestEnable = sendHeartbeatRequestEnable;
	}



	public void setHeartbeatSeconds(int heartbeatSeconds){
		this.heartbeatSeconds = heartbeatSeconds;
		super.setDelay(heartbeatSeconds);
	}
	
	public void setMaxWaitCount(int maxWaitCount){
		this.maxWaitCount = maxWaitCount;
	}

	public int getHeartbeatSeconds() {
		return heartbeatSeconds;
	}

	public int getMaxWaitCount() {
		return maxWaitCount;
	}

	private void heartbeatCheck(){
		Collection<Session> sessions = hydra.getSessions();
        if(LOGGER.isDebugEnabled()){
		    LOGGER.debug("["+getName()+"] : Found alive sessions("+sessions.size()+")");
        }
		try{
			for(Session session : sessions){
				if(sendHeartbeatRequestEnable){
					session.sendHeatbeatRequest();//发送心跳请求
				}
				//检查心跳
				Long time = heatbeatTimeMap.get(session.getSessionId());
				if(time != null){
					if(System.currentTimeMillis() - time > heartbeatSeconds * 1000 * maxWaitCount ){ //如果连继maxWaitCount次以上没收到过心跳，则认为断线
						session.close();
						LOGGER.warn("["+getName()+"]:The session heartbeat timeout,closing - " + session);
					}
				}
			}
		}catch(Throwable e){
			LOGGER.warn("["+getName()+"] :"+e.getMessage());
		}
	}

	@Override
	public void run() {
		heartbeatCheck();
	}

	public void onHeartbeat(Session session) {
		heatbeatTimeMap.put(session.getSessionId(), System.currentTimeMillis());
	}
}
