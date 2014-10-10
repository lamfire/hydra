package com.lamfire.hydra;


import com.lamfire.hydra.Message;

/**
 * 消息处理接口
 * @author lamfire
 *
 */
public interface Action{

	public abstract void execute(MessageContext context, Message message);

}
