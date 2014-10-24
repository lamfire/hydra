package com.lamfire.hydra.reply;

import com.lamfire.hydra.Message;
import com.lamfire.hydra.exception.HydraException;
import com.lamfire.logger.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: lamfire
 * Date: 14-2-17
 * Time: 下午6:04
 * To change this template use File | Settings | File Templates.
 */
class ReplyFuture {

    private static final Logger LOGGER = Logger.getLogger(ReplyFuture.class);
    private int messageId;
    private long readTimeoutMillis = 120000;
    private Message message;


    public ReplyFuture(int messageId) {
        this.messageId = messageId;
    }

    public long getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(long readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public synchronized void await(){
        await(readTimeoutMillis);
    }

    public synchronized void await(long millis){
        try {
            if (message == null) {
                this.wait(millis);
            }
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(),e);
            throw new HydraException(e.getMessage(), e);
        }
    }

    synchronized void onReceivedReply(Message message) {
        this.message = message;
        this.notifyAll();
    }

    public byte[] getReply(){
        if(this.message == null){
            await();
        }

        if(this.message == null){
            throw new HydraException("Read response timeout.");
        }
        return this.message.getBody();
    }
}
