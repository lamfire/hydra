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
    private int _IoBossThreads = 1;
    private int _IoWorkerThreads = 4;
    private int _ServiceThreads = 4;
    private int _ScheduledTaskThreads = 1;

    private ExecutorService _IoBossExecutor;
    private ExecutorService _IoWorkerExecutor;
    private ExecutorService _ServiceExecutor;
    private ScheduledThreadPoolExecutor _ScheduledExecutor;

    private static final HydraExecutorMgr instance = new  HydraExecutorMgr();

    public static final HydraExecutorMgr getInstance(){
        return instance;
    }

    private HydraExecutorMgr(){

    }

    public int getIoBossThreads() {
        return _IoBossThreads;
    }

    public int getIoWorkerThreads() {
        return _IoWorkerThreads;
    }

    public int getServiceThreads() {
        return _ServiceThreads;
    }

    public int getScheduledTaskThreads() {
        return _ScheduledTaskThreads;
    }

    public void setIoBossThreads(int ioBossThreads) {
        this._IoBossThreads = ioBossThreads;
    }

    public void setServiceThreads(int serviceThreads) {
        this._ServiceThreads = serviceThreads;
    }

    public void setIoWorkerThreads(int ioWorkerThreads) {
        this._IoWorkerThreads = ioWorkerThreads;
    }

    public void setScheduledTaskThreads(int scheduledTaskThreads) {
        this._ScheduledTaskThreads = scheduledTaskThreads;
    }

    synchronized ExecutorService getIoBossExecutor() {
        if(_IoBossExecutor == null){
            LOGGER.info("[CREATE_EXECUTOR] : Create IO boss executor - " + _IoBossThreads);
            _IoBossExecutor = Executors.newFixedThreadPool(_IoBossThreads, Threads.makeThreadFactory("BOSS"));
        }
        return _IoBossExecutor;
    }

    synchronized ExecutorService getIoWorkerExecutor() {
        if(_IoWorkerExecutor == null){
            LOGGER.info("[CREATE_EXECUTOR] : Create IO worker executor - " + _IoWorkerThreads);
            _IoWorkerExecutor = Executors.newFixedThreadPool(_IoWorkerThreads, Threads.makeThreadFactory("WORKER"));
        }
        return _IoWorkerExecutor;
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

    public void shutdownIoBossExecutor(){
        if(this._IoBossExecutor != null){
            LOGGER.info("[SHUTDOWN] : Shutdown IO boss executor");
            this._IoBossExecutor.shutdown();;
            this._IoBossExecutor = null;
        }
    }

    public void shutdownIoWorkerExecutor(){
        if(this._IoWorkerExecutor != null){
            LOGGER.info("[SHUTDOWN] : Shutdown IO worker executor");
            this._IoWorkerExecutor.shutdown();
            this._IoWorkerExecutor = null;
        }
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
        shutdownIoBossExecutor() ;
        shutdownIoWorkerExecutor();
        shutdownServiceExecutor();
    }
}
