package com.lamfire.hydra;

import com.lamfire.logger.Logger;
import com.lamfire.utils.Sets;

import java.util.Set;

/**
 * 自动重连任务
 */
class AutoConnectTask extends HydraTask {
	private static final Logger LOGGER = Logger.getLogger(AutoConnectTask.class);

    private static final AutoConnectTask instance = new AutoConnectTask();

    public static AutoConnectTask getInstance(){
        return instance;
    }

	private final Set<Hydra> hydras = Sets.newHashSet();

	private AutoConnectTask() {
		super("AUTOCONNECT");
	}

    public void add(Hydra hydra){
         hydras.add(hydra);
    }

    public void remove(Hydra hydra){
        hydras.remove(hydra);
    }

    private void reconnects(Hydra hydra ,int conns){
        try {
            for (int i = 0; i < conns; i++) {
                hydra.connect();
            }
        }catch (Throwable e){
            LOGGER.error("[FAILED] : "+e.getMessage() +" - " + hydra,e);
        }
    }


	public void executeTask(Hydra hydra) {
		try {
			int conns = hydra.getSessions().size();
            int  keepaliveConnections = hydra.getKeepaliveConnsWithClient();

            if(LOGGER.isDebugEnabled()){
			    LOGGER.debug("[STATUS]:[" + hydra + "] connected=" + conns + "/" + keepaliveConnections);
            }
			if (conns < keepaliveConnections) {
				int count = keepaliveConnections - conns;
                LOGGER.info("Trying to connect to [" + hydra + "],connected=" + conns + "/" + keepaliveConnections );
                if (count > 8) {
					count /= 3;
				}
				reconnects(hydra ,count);
			}
		} catch (Throwable e) {
			LOGGER.error("[EXCEPTION]:"+e.getMessage() +" - " + hydra);
		}
	}

    @Override
    public void run() {
         for(Hydra hydra : hydras){
             executeTask(hydra);
         }
    }

}
