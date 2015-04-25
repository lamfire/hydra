package com.lamfire.hydra.reply;

import com.lamfire.hydra.*;
import com.lamfire.hydra.exception.HydraException;
import com.lamfire.logger.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: lamfire
 * Date: 14-2-17
 * Time: 下午6:02
 * To change this template use File | Settings | File Templates.
 */
public class ReplySnake extends Snake{
    private static final Logger LOGGER = Logger.getLogger(ReplySnake.class);
    private ReplyWaitQueue replyQueue = new ReplyWaitQueue();
    private AtomicInteger atimic = new AtomicInteger();
    private OnReceivedPushMessageListener onReceivedPushMessageListener;
    private long readTimeoutMills = 120000;

    public ReplySnake(String host, int port) {
        super(host, port);
    }

    public Future sendOnly(byte[] bytes){
        int id = atimic.getAndIncrement();
        Session session =awaitAvailableSession();
        if(session == null){
            throw new HydraException("No available sessions") ;
        }
        return session.send(new Message(id, bytes));
    }

    public long getReadTimeoutMills() {
        return readTimeoutMills;
    }

    public void setReadTimeoutMills(long readTimeoutMills) {
        this.readTimeoutMills = readTimeoutMills;
    }

    public byte[] send(byte[] bytes){
        int id = atimic.getAndIncrement();
        try{
            ReplyFuture future = new ReplyFuture(id);
            future.setReadTimeoutMillis(readTimeoutMills);
            replyQueue.add(future);
            Session session = awaitAvailableSession();
            if(session == null){
                throw new HydraException("No available sessions") ;
            }
            session.send(new Message(id,bytes)).awaitUninterruptibly();
            byte[] result = future.getReply();
            return result;
        }finally{
            replyQueue.remove(id);
        }
    }

    public byte[] send(byte[] bytes,long timeout){
        int id = atimic.getAndIncrement();
        try{
            ReplyFuture future = new ReplyFuture(id);
            future.setReadTimeoutMillis(timeout);
            replyQueue.add(future);
            Session session = awaitAvailableSession();
            session.send(new Message(id,bytes)).awaitUninterruptibly();
            byte[] result = future.getReply();
            return result;
        }finally{
            replyQueue.remove(id);
        }
    }

    @Override
    protected void handleMessage(MessageContext context, Message message) {
        ReplyFuture future = replyQueue.take(message.getId());
        if(future == null){
            onReceivedPushMessage(context,message);
            return;
        }
        future.onReceivedReply(message);
    }

    protected void onReceivedPushMessage(MessageContext context, Message message){
        if(this.onReceivedPushMessageListener != null){
            this.onReceivedPushMessageListener.onReceivedPushMessage(context,message);
            return;
        }
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Not overwrite method onReceivedPushMessage.ignore message:" + message);
        }
    }

    public void setOnReceivedPushMessageListener(OnReceivedPushMessageListener onReceivedPushMessageListener) {
        this.onReceivedPushMessageListener = onReceivedPushMessageListener;
    }
}
