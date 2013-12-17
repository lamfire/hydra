package com.lamfire.hydra.sample.reqres;


import com.lamfire.hydra.exception.HydraException;
import com.lamfire.logger.Logger;
import com.lamfire.hydra.Message;

public class ResponseFuture {
    private static final Logger LOGGER = Logger.getLogger(ResponseFuture.class);
	private int messageId;
	private long readTimeoutMillis = 15000;
	private Message message;


	public ResponseFuture(int messageId) {
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

	synchronized void onResponse(Message message) {
		this.message = message;
		this.notifyAll();
	}
	
	public byte[] getResponse(){
		if(this.message == null){
			await();
		}
		
		if(this.message == null){
			throw new HydraException("Read response timeout.");
		}
		return this.message.getBody();
	}

}
