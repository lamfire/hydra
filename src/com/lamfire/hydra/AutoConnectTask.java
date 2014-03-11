package com.lamfire.hydra;

import com.lamfire.logger.Logger;

/**
 * 自动重连任务
 */
class AutoConnectTask extends HydraTask {
	private static final Logger LOGGER = Logger.getLogger(AutoConnectTask.class);
	private Hydra hydra;
	private int keepaliveConnections = 1;
	
	public AutoConnectTask(Hydra hydra) {
		super("AUTOCONNECT");
		this.hydra = hydra;
	}
	
	

	public int getKeepaliveConnections() {
		return keepaliveConnections;
	}



	public void setKeepaliveConnections(int keepaliveConnections) {
		this.keepaliveConnections = keepaliveConnections;
	}

    private void reconnects(int conns){
        try {
            for (int i = 0; i < conns; i++) {
                hydra.connect();
            }
            LOGGER.info("[SUCCESS]:reconnected("+conns+") to [" + hydra.getHost() + ":" + hydra.getPort()+"]");
        }catch (Throwable e){
            LOGGER.error("[FAILED]:"+e.getMessage());
        }
    }

	@Override
	public void run() {
		try {
			String host = hydra.getHost();
			int port = hydra.getPort();
			int conns = hydra.getSessions().size();
            if(LOGGER.isDebugEnabled()){
			    LOGGER.debug("[Connection Check]:The connections current[" + conns + "],keepalive[" + keepaliveConnections + "]");
            }
			if (conns < keepaliveConnections) {
				int count = keepaliveConnections - conns;
                LOGGER.info("[CONNECTING]:Try to reconnect to [" + host + ":" + port + "],connected[" + conns + "],keepalive[" + keepaliveConnections + "]");
                if (count > 8) {
					count /= 3;
				}
				reconnects(count);
			}
		} catch (Throwable e) {
			LOGGER.error("[EXCEPTION]:"+e.getMessage());
		}
	}

}
