package com.lamfire.hydra;

import com.lamfire.logger.Logger;
import com.lamfire.utils.ThreadFactory;
import com.lamfire.utils.Threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Hydra线程管理器
 * User: lamfire
 * Date: 14-10-30
 * Time: 上午10:08
 * To change this template use File | Settings | File Templates.
 */
public class HydraExecutorMgr {
    private static final Logger LOGGER = Logger.getLogger(HydraExecutorMgr.class);
    private int _ServiceThreads = 4;
    private int _ScheduledTaskThreads = 1;

    private ExecutorService _ServiceExecutor;
    private ScheduledThreadPoolExecutor _ScheduledExecutor;

    private static final HydraExecutorMgr instance = new  HydraExecutorMgr();

    public static final HydraExecutorMgr getInstance(){
        return instance;
    }

    private HydraExecutorMgr(){

    }

    public int getServiceThreads() {
        return _ServiceThreads;
    }

    public int getScheduledTaskThreads() {
        return _ScheduledTaskThreads;
    }

    public void setServiceThreads(int serviceThreads) {
        this._ServiceThreads = serviceThreads;
    }

    public void setScheduledTaskThreads(int scheduledTaskThreads) {
        this._ScheduledTaskThreads = scheduledTaskThreads;
    }

    synchronized ExecutorService getServiceExecutor() {
        if(_ServiceExecutor == null){
            LOGGER.info("[CREATE_EXECUTOR] : Create service executor - " + _ServiceThreads);
            _ServiceExecutor = Executors.newFixedThreadPool(_ServiceThreads, Threads.makeThreadFactory("SERVICE"));
        }
        return _ServiceExecutor;
    }

    synchronized ScheduledThreadPoolExecutor getScheduledExecutor() {
        if(_ScheduledExecutor == null){
            LOGGER.info("[CREATE_EXECUTOR] : Create scheduled executor - " + _ScheduledTaskThreads);
            _ScheduledExecutor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(_ScheduledTaskThreads,new ThreadFactory("HYDRA_SCHEDULE_TASK"));
        }
        return _ScheduledExecutor;
    }

    public void shutdownServiceExecutor(){
        if(_ServiceExecutor != null){
            LOGGER.info("[SHUTDOWN] : Shutdown service executor");
            this._ServiceExecutor.shutdown();
            this._ServiceExecutor = null;
        }
    }

    public void shutdownScheduledExecutor(){
        if(_ScheduledExecutor != null){
            LOGGER.info("[SHUTDOWN] : Shutdown scheduled executor");
            this._ScheduledExecutor.shutdown();
            this._ScheduledExecutor = null;
        }
    }

    public void shutdownAll(){
        shutdownScheduledExecutor();
        shutdownServiceExecutor();
    }
}
