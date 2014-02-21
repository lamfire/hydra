package com.lamfire.hydra;

/**
 * 消息调度目的地
 * User: lamfire
 * Date: 14-2-21
 * Time: 上午10:21
 * To change this template use File | Settings | File Templates.
 */
public interface Destination {
    public void forwardMessage(Message message);
}
