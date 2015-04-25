package com.lamfire.hydra.reply;

import com.lamfire.hydra.Message;
import com.lamfire.hydra.MessageContext;

/**
 * 收到推送消息侦听器
 * User: lamfire
 * Date: 14-2-27
 * Time: 下午4:09
 * To change this template use File | Settings | File Templates.
 */
public interface OnReceivedPushMessageListener {
    void onReceivedPushMessage(MessageContext context, Message message);
}
