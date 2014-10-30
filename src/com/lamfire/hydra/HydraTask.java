package com.lamfire.hydra;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lamfire.logger.Logger;
import com.lamfire.utils.ThreadFactory;

abstract class HydraTask implements Runnable{
	private static final Logger LOGGER = Logger.getLogger(HydraTask.class);
	private String name;
	private long delay = 10;
    private boolean _starting = false;
	
	public HydraTask(String name){
		this(name,10);
	}
	
	public HydraTask(String name, long delay){
		this.name = name;
		this.delay = delay;
	}

	public String getName() {
		return name;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public synchronized void startup(){
        if(_starting){
            return;
        }
        if(LOGGER.isDebugEnabled()){
		    LOGGER.debug("["+name+"@HydraTask]:startup schedule with delay " + delay + " seconds");
        }
        HydraExecutorMgr.getInstance().getScheduledExecutor().scheduleWithFixedDelay(this, this.delay, this.delay, TimeUnit.SECONDS);
        _starting = true;
	}
	
	public void shutdown(){
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("["+name+"@HydraTask]:shutdown schedule with delay " + delay + " seconds");
        }
        HydraExecutorMgr.getInstance().getScheduledExecutor().remove(this);
        _starting = false;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HydraTask hydraTask = (HydraTask) o;

        if (name != null ? !name.equals(hydraTask.name) : hydraTask.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
