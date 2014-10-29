package com.lamfire.hydra;

import com.lamfire.logger.Logger;

/**
 * 自动重连任务
 */
class AutoConnectTask extends HydraTask {
	private static final Logger LOGGER = Logger.getLogger(AutoConnectTask.class);
	private Hydra hydra;
    private boolean rebooted = false;
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

    private void reboot(){
        String host = hydra.getHost();
        int port = hydra.getPort();
        LOGGER.debug("[REBOOTING]:[" + host + ":" + port + "]");
        this.hydra.shutdown();
    }

    private void reconnects(int conns){
        try {
            for (int i = 0; i < conns; i++) {
                Session session = hydra.connect();
                if(session.isConnected()){
                    this.rebooted = false;
                }
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

            if(conns == 0 && rebooted == false){
                reboot();
                this.rebooted = true;
            }

            if(LOGGER.isDebugEnabled()){
			    LOGGER.debug("[RECONNECT STATUS]:[" + host + ":" + port + "] connected=" + conns + "/" + keepaliveConnections);
            }
			if (conns < keepaliveConnections) {
				int count = keepaliveConnections - conns;
                LOGGER.info("[RECONNECTING]:Try to reconnect to [" + host + ":" + port + "],connected=" + conns + "/" + keepaliveConnections );
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
