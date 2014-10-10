package com.lamfire.hydra;

import java.nio.ByteBuffer;

public interface MessageHandler {

	public void onMessageReceived(Context context,Session session,Message message);
}
