package com.lamfire.hydra;

import com.lamfire.logger.Logger;

/**
 * 自动重连任务
 */
class AutoConnectTask extends HydraTask {
	private static final Logger LOGGER = Logger.getLogger(AutoConnectTask.class);
	private Hydra river;
	private int keepaliveConnections = 1;
	
	public AutoConnectTask(Hydra river) {
		super("AUTOCONNECT");
		this.river = river;
	}
	
	

	public int getKeepaliveConnections() {
		return keepaliveConnections;
	}



	public void setKeepaliveConnections(int keepaliveConnections) {
		this.keepaliveConnections = keepaliveConnections;
	}



	@Override
	public void run() {
		try {
			String host = river.getHost();
			int port = river.getPort();
			int conns = river.getSessions().size();
			LOGGER.debug("[AUTOCONNECT]:The connections current[" + conns + "],keepalive[" + keepaliveConnections + "]");
			if (conns < keepaliveConnections) {
				int count = keepaliveConnections - conns;
				if (count > 8) {
					count /= 3;
					for (int i = 0; i < count; i++) {
						LOGGER.info("[AUTOCONNECT]:The connections current[" + conns + "],keepalive[" + keepaliveConnections + "],try connect to " + host + ":" + port);
						river.connect();
					}
				} else {
					LOGGER.info("[AUTOCONNECT]:The connections current[" + conns + "],keepalive[" + keepaliveConnections + "],try connect to " + host + ":" + port);
					river.connect();
				}
			}
		} catch (Exception e) {
			LOGGER.error("[AUTOCONNECT]:"+e.getMessage(),e);
		}
	}

}
