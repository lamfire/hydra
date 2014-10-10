package com.lamfire.hydra.reply;

import com.lamfire.hydra.CycleSessionIterator;
import com.lamfire.hydra.Message;
import com.lamfire.hydra.MessageContext;
import com.lamfire.hydra.Snake;
import com.lamfire.hydra.Session;
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
    private CycleSessionIterator it;
    private AtomicInteger atimic = new AtomicInteger();
    private OnReceivedPushMessageListener onReceivedPushMessageListener;

    public ReplySnake(String host, int port) {
        super(host, port);
        it = new CycleSessionIterator(this);
    }

    public ReplyFuture send(byte[] bytes){
        int id = atimic.getAndIncrement();
        ReplyFuture future = new ReplyFuture(id);
        replyQueue.add(future);
        Session session = it.nextAvailableSession();
        session.send(new Message(id,bytes));
        return future;
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
