package com.lamfire.hydra.reply;

import com.lamfire.hydra.CycleSessionIterator;
import com.lamfire.hydra.Message;
import com.lamfire.hydra.MessageContext;
import com.lamfire.hydra.Snake;
import com.lamfire.hydra.net.Session;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: lamfire
 * Date: 14-2-17
 * Time: 下午6:02
 * To change this template use File | Settings | File Templates.
 */
public class ReplySnake extends Snake{
    private ReplyWaitQueue replyQueue = new ReplyWaitQueue();
    private CycleSessionIterator it;
    private AtomicInteger atimic = new AtomicInteger();

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
        if(future != null){
            future.onReceivedReply(message);
        }
    }

}
