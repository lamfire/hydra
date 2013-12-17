package com.lamfire.hydra;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.lamfire.logger.Logger;
import com.lamfire.utils.ThreadFactory;

abstract class HydraTask implements Runnable{
	private static final Logger LOGGER = Logger.getLogger(HydraTask.class);
	private ScheduledExecutorService schedule;
	private String name;
	private long delay = 10;
	
	public HydraTask(String name){
		this.name = name;
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
		if(schedule != null){
			return;
		}
		schedule = Executors.newScheduledThreadPool(1,new ThreadFactory(name));
		LOGGER.debug("["+name+"@NkitTask]:startup schedule with delay " + delay + " seconds");
		schedule.scheduleWithFixedDelay(this, this.delay, this.delay, TimeUnit.SECONDS);
	}
	
	public void shutdown(){
		if(schedule == null){
			return;
		}
		schedule.shutdown();
	}
}
